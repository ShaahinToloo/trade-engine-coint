import pandas as pd
import numpy as np
import MetaTrader5 as mt5
import sys
from datetime import datetime, timedelta, timezone
import time
from selenium import webdriver
from bs4 import BeautifulSoup
from logger import TradingLogger
import sys


GMT_OFFSET = None
OVERALL_BALANCE = None

daily_balance = None
news_items = []


def set_gmt_offset(gmt_offset):
    global GMT_OFFSET
    gmt_offset = float(gmt_offset)
    GMT_OFFSET = gmt_offset


def set_overall_balance(overall_balance):
    global OVERALL_BALANCE
    overall_balance = float(overall_balance)
    OVERALL_BALANCE = overall_balance


logger = TradingLogger("XAUUSD", GMT_OFFSET)


def login():
    account = 13394426
    password = "zgfTZ94##"
    server = "FundedNext-Server 2"

    if not mt5.initialize():
        print("Failed to initialize MT5. Exiting...")
        mt5.shutdown()
        return False

    if not mt5.login(account, password, server):
        print("Login failed, error code:", mt5.last_error())
        return False
    else:
        print("Logged in successfully")
    return True


class news:
    def __init__(self):
        self.news_items = []

    def fetch_metalsmine_news(self):
        """
        Fetch high-impact news from MetalsMine calendar.
        Returns news times in local timezone.
        """
        max_retries = 3
        retry_delay = 5  # seconds

        for attempt in range(max_retries):
            try:
                url = "https://www.metalsmine.com/calendar"

                options = webdriver.ChromeOptions()
                options.add_argument("--headless=new")
                options.add_argument("--disable-gpu")
                options.add_argument("--no-sandbox")
                options.add_argument("--disable-dev-shm-usage")
                options.add_argument("--enable-unsafe-swiftshader")
                options.add_argument("--window-size=1920,1080")
                options.add_argument(
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                options.add_argument("--log-level=3")
                options.add_experimental_option(
                    'excludeSwitches', ['enable-logging'])
                options.add_argument("--disable-usb-keyboard-detect")

                driver = webdriver.Chrome(options=options)
                driver.implicitly_wait(15)

                # Load page with retry logic
                driver.get(url)
                wait_time = 10
                for _ in range(3):
                    time.sleep(wait_time)
                    page_source = driver.page_source
                    if 'calendar__table' in page_source:
                        break
                    wait_time += 5

                soup = BeautifulSoup(page_source, 'html.parser')

                today = datetime.now().date()
                # Today's month/day string for comparison e.g., "Apr 7"
                today_month_day = today.strftime("%b %#d")
                # If %#d doesn't work on all Windows systems then try using "%b %d" and strip leading zeros.

                calendar_table = soup.find("table", class_="calendar__table")
                if not calendar_table:
                    logger.error(
                        f"Calendar table not found on attempt {attempt + 1}")
                    if attempt < max_retries - 1:
                        time.sleep(retry_delay)
                        continue
                    driver.quit()

                rows = calendar_table.find_all("tr", class_="calendar__row")
                self.news_items = []
                process_this_section = False  # Flag to denote if we are in today's news section
                last_valid_time = None  # Track the last valid time

                for row in rows:
                    # Check if the row contains a date cell.
                    date_cell = row.find("td", class_="calendar__date")
                    if date_cell:
                        # e.g., "WedApr9" or "MonApr7"
                        date_text = date_cell.get_text(strip=True)
                        if today_month_day in date_text:
                            process_this_section = True
                            logger.info(f"Processing date section: {date_text}")
                        else:
                            process_this_section = False
                        # Move to the next row after handling date row.
                        continue

                    if not process_this_section:
                        continue

                    # Update last_valid_time if the row has any time cell value.
                    time_cell = row.find("td", class_="calendar__time")
                    if time_cell and time_cell.text.strip():
                        last_valid_time = time_cell.text.strip()

                    # Process only rows with high-impact news.
                    impact_cell = row.find("td", class_="calendar__impact")
                    if impact_cell and "high" in str(impact_cell).lower():
                        if not last_valid_time:
                            continue

                        event_time = last_valid_time
                        if event_time.lower() == "all day":
                            continue

                        try:
                            event_time = event_time.lower().replace('am', ' AM').replace('pm', ' PM')
                            if len(event_time.split(':')[0]) == 1:
                                event_time = '0' + event_time

                            event_datetime = datetime.strptime(
                                f"{today.strftime('%Y-%m-%d')} {event_time}",
                                "%Y-%m-%d %I:%M %p"
                            )

                            title_cell = row.find("td", class_="calendar__event")
                            event_title = title_cell.text.strip() if title_cell else "Unknown Event"

                            self.news_items.append({
                                "time": event_datetime,
                                "title": event_title
                            })
                            logger.info(
                                f"Found high-impact news: {event_title} at {event_datetime}")
                        except ValueError as e:
                            logger.error(f"Error parsing time '{event_time}': {e}")
                            continue

                driver.quit()

            except Exception as e:
                logger.error(
                    f"Error in fetch_metalsmine_news attempt {attempt + 1}: {str(e)}")
                if attempt < max_retries - 1:
                    time.sleep(retry_delay)
                    continue

        for new in self.news_items:
            new_time = new["time"]
            new_title = new["title"]
            logger.info(f"\n{new_time} | {new_title}\n")


    def _is_trade_allowed(self):
        for news_item in self.news_items:
            event_time = news_item['time']
            before = event_time - timedelta(minutes=90)
            after = event_time + timedelta(minutes=20)

            # If the current time is within the restricted window, stop trading
            current_time = datetime.now(timezone(timedelta(hours=GMT_OFFSET)))
            if before <= current_time <= after:
                seconds_remaining = (after - current_time).total_seconds()
                logger.warning(
                    f"Trading is restricted due to news event at {event_time.strftime('%Y-%m-%d %H:%M:%S GMT+3')}. {seconds_remaining:.0f} seconds remaining.")
                return False, seconds_remaining
        return True, 0


    def news_sleep_until_allowance(self):
        is_allowed, seconds_remaining = self._is_trade_allowed()
        if not is_allowed:
            time.sleep(seconds_remaining)
            return False
        return True


def account_balance():
    try:
        # Check if MT5 is initialized
        if not mt5.initialize():
            logger.warning(
                "MT5 is not initialized. Attempting to initialize...")
            sleep_list = [0, 3, 10, 30, 60, 300, 900, 1800, 3600]
            step = 0
            while step < 10:
                if mt5.initialize():
                    logger.info(f"initialized MT5 after {step} steps")
                    break
                else:
                    step += 1
                    logger.error(f"Failed to initialize MT5, step: {step}")
                    time.sleep(sleep_list[step])
            else:
                logger.error(
                    f"Failed to initialize MT5 at all. After taking {step} steps.\n  Shutting Down")
                sys.exit()

        # Get account info
        account_info = mt5.account_info()
        if account_info is None:
            logger.error(
                "Failed to get account info. Checking connection status...")
            # Check connection status
            if not mt5.terminal_info():
                logger.error("MT5 terminal is not connected")
                return None
            logger.error(
                "MT5 terminal is connected but account info is not available")
            return None

        balance = account_info.balance
        # logger.account(f"Account balance: ${balance:,.2f}")
        return balance
    except Exception as e:
        logger.error(f'Error fetching account_info: {e}')
        logger.error(f'MT5 connection status: {mt5.terminal_info()}')
        return None


def terminate_all_positions():
    """
    Closes all open market positions and cancels all pending orders across all symbols.
    """
    logger.info(f"Attempting to close all trades and cancel all orders.")

    # --- Close Open Positions ---
    positions = mt5.positions_get()  # Get positions for all symbols
    if positions is None:
        if mt5.last_error()[0] != 1:  # Ignore "no orders" error
            logger.error(f"Failed to get positions, error: {mt5.last_error()}")
        else:
            logger.info(f"No open positions found.")

    elif len(positions) > 0:
        logger.info(
            f"Found {len(positions)} open positions across all symbols. Closing...")
        for position in positions:
            symbol = position.symbol  # Get symbol from the position object
            order_type = mt5.ORDER_TYPE_SELL if position.type == mt5.POSITION_TYPE_BUY else mt5.ORDER_TYPE_BUY
            price = mt5.symbol_info_tick(
                symbol).bid if position.type == mt5.POSITION_TYPE_BUY else mt5.symbol_info_tick(symbol).ask
            volume = position.volume

            close_request = {
                "action": mt5.TRADE_ACTION_DEAL,
                "symbol": symbol,
                "volume": volume,
                "type": order_type,
                "position": position.ticket,
                "price": price,
                "deviation": 20,  # Allow some slippage
                "comment": "Close All Weekend",
                "type_time": mt5.ORDER_TIME_GTC,
                "type_filling": mt5.ORDER_FILLING_IOC,  # Or FOK, depends on broker
            }

            result = mt5.order_send(close_request)
            if result and result.retcode == mt5.TRADE_RETCODE_DONE:
                logger.trade(
                    f"Successfully closed position {position.ticket} for {symbol}.")
            else:
                logger.error(
                    f"Failed to close position {position.ticket} for {symbol}. Retcode: {result.retcode if result else 'N/A'}, Error: {mt5.last_error()}")
    else:
        logger.info(f"No open positions found.")

    # --- Cancel Pending Orders ---
    orders = mt5.orders_get()  # Get orders for all symbols
    if orders is None:
        if mt5.last_error()[0] != 1:  # Ignore "no orders" error
            logger.error(
                f"Failed to get pending orders, error: {mt5.last_error()}")
        else:
            logger.info(f"No pending orders found.")

    elif len(orders) > 0:
        logger.info(
            f"Found {len(orders)} pending orders across all symbols. Cancelling...")
        for order in orders:
            symbol = order.symbol  # Get symbol from the order object
            cancel_request = {
                "action": mt5.TRADE_ACTION_REMOVE,
                "order": order.ticket,
                "symbol": symbol,
            }
            result = mt5.order_send(cancel_request)
            if result and result.retcode == mt5.TRADE_RETCODE_DONE:
                logger.trade(
                    f"Successfully cancelled order {order.ticket} for {symbol}.")
            else:
                logger.error(
                    f"Failed to cancel order {order.ticket} for {symbol}. Retcode: {result.retcode if result else 'N/A'}, Error: {mt5.last_error()}")
    else:
        logger.info(f"No pending orders found.")

    logger.info(f"Finished closing/cancelling process for all symbols.")


def get_symbol_info(symbol):
    if not mt5.initialize():
        logger.error("Failed to initialize MT5")
        return 0.0

    symbol_info = mt5.symbol_info(symbol)

    if symbol_info is None:
        logger.error(f"Failed to get symbol info for {symbol}")
        return 0.0
    return symbol_info._asdict()


def check_for_conflicting_order(symbol, entry, sl, tp) -> bool:
    tolerance = 0.01

    # ----- 1. Check Pending Orders -----
    pending_orders = mt5.orders_get(symbol=symbol)
    if pending_orders:
        for order in pending_orders:
            if abs(order.price_open - entry) < tolerance and \
                    abs(order.sl - sl) < tolerance and \
                    abs(order.tp - tp) < tolerance:

                logger.info(
                    f"Conflicting pending order {order.ticket} found. Skipping new order.")
                return True

    # ----- 2. Check Recently Filled Orders (e.g., past 168 hour -> 7 days) -----
    now = datetime.now()
    one_hour_ago = now - timedelta(hours=168)
    filled_orders = mt5.history_orders_get(one_hour_ago, now)

    if filled_orders:
        for order in filled_orders:
            if order.symbol != symbol:
                continue
            if abs(order.price_open - entry) < tolerance and \
                    abs(order.sl - sl) < tolerance and \
                    abs(order.tp - tp) < tolerance:

                logger.info(
                    f"Conflicting filled order {order.ticket} found. Skipping new order.")
                return True

    return False


def place_order_with_backoff(request, max_retries=4):
    delay_times = []  # in Seconds
    for i in range(1, max_retries+1):
        delay_times.append(i*3)
    attempt = 0

    while attempt < max_retries:
        result = mt5.order_send(request)
        if result and result.retcode == mt5.TRADE_RETCODE_DONE:
            logger.trade(f"Order successfully placed: {result.order}")
            return result
        else:
            logger.warning(
                f"Order failed, attempt {attempt + 1}/{max_retries}. Retrying in {delay_times[attempt]} seconds...")
            attempt += 1
            if attempt < max_retries:
                time.sleep(delay_times[attempt])

    logger.error(f"Failed to place order after {max_retries} attempts.")
    return None


def execute_order(signal, volume, symbol, entry, sl, tp):
    if signal == 1:
        request_limit = {
            "action": mt5.TRADE_ACTION_PENDING,
            "symbol": symbol,
            "volume": volume,
            "type": mt5.ORDER_TYPE_BUY_LIMIT,
            "price": entry,
            "sl": sl,
            "tp": tp,
            "deviation": 10,
            "magic": 1,
            "comment": f"Buy Limit @ {entry:.2f}",
            "type_time": mt5.ORDER_TIME_GTC,
            "type_filling": mt5.ORDER_FILLING_RETURN,
        }

        result_limit = place_order_with_backoff(request_limit)

        if result_limit:
            logger.trade(
                f"Buy LIMIT order placed: Entry={entry:.2f}, SL={sl:.2f}, TP={tp:.2f}, Volume={volume} lots, Order ID: {result_limit.order}")
            time.sleep(1800)
        else:
            logger.warning(
                f"Buy LIMIT order FAILED, trying BUY STOP instead...")

            request_stop = {
                "action": mt5.TRADE_ACTION_PENDING,
                "symbol": symbol,
                "volume": volume,
                "type": mt5.ORDER_TYPE_BUY_STOP,
                "price": entry,
                "sl": sl,
                "tp": tp,
                "deviation": 10,
                "magic": 1,
                "comment": f"Buy Stop @ {entry:.2f}",
                "type_time": mt5.ORDER_TIME_GTC,
                "type_filling": mt5.ORDER_FILLING_RETURN,
            }

            result_stop = place_order_with_backoff(request_stop)

            if result_stop:
                logger.trade(
                    f"Buy STOP order placed: Trigger={entry:.2f}, SL={sl:.2f}, TP={tp:.2f}, Volume={volume} lots, Order ID: {result_stop.order}")
                time.sleep(1800)
            else:
                logger.error(
                    f"Buy STOP order FAILED after retries.")

    elif signal == 0:
        request_limit = {
            "action": mt5.TRADE_ACTION_PENDING,
            "symbol": symbol,
            "volume": volume,
            "type": mt5.ORDER_TYPE_SELL_LIMIT,
            "price": entry,
            "sl": sl,
            "tp": tp,
            "deviation": 10,
            "magic": 0,
            "comment": f"Sell Limit @ {entry:.2f}",
            "type_time": mt5.ORDER_TIME_GTC,
            "type_filling": mt5.ORDER_FILLING_RETURN,
        }

        result_limit = place_order_with_backoff(request_limit)

        if result_limit:
            logger.trade(
                f"Sell LIMIT order placed: Entry={entry:.2f}, SL={sl:.2f}, TP={tp:.2f}, Volume={volume} lots, Order ID: {result_limit.order}")
            time.sleep(1800)
        else:
            logger.warning(
                f"Sell LIMIT order FAILED, trying SELL STOP instead...")

            request_stop = {
                "action": mt5.TRADE_ACTION_PENDING,
                "symbol": symbol,
                "volume": volume,
                "type": mt5.ORDER_TYPE_SELL_STOP,
                "price": entry,
                "sl": sl,
                "tp": tp,
                "deviation": 10,
                "magic": 0,
                "comment": f"Sell Stop @ {entry:.2f}",
                "type_time": mt5.ORDER_TIME_GTC,
                "type_filling": mt5.ORDER_FILLING_RETURN,
            }

            result_stop = place_order_with_backoff(request_stop)

            if result_stop:
                logger.trade(
                    f"Sell STOP order placed: Trigger={entry:.2f}, SL={sl:.2f}, TP={tp:.2f}, Volume={volume} lots, Order ID: {result_stop.order}")
                time.sleep(1800)
            else:
                logger.error(
                    f"Sell STOP order FAILED after retries.")
                

# def log_result(success, msg, retcode):
#     if success:
#         logger.trade(msg)
#     else:
#         logger.error(f"{msg}, retcode={retcode}")


# def check_and_cancel_orders(symbol, high, low):
#     high = np.array(high).astype(np.float32).tolist()
#     low = np.array(low).astype(np.float32).tolist()

#     # latest_trend = df15.iloc[-1]['Trend']
#     orders = mt5.orders_get(symbol=symbol)
#     if orders is None:
#         return

#     for order in orders:
#         order_type = order.type
#         sl_price = order.sl
#         order_time = order.time_setup  # UNIX timestamp
#         order_dt = datetime.fromtimestamp(order_time)
#         current_time = datetime.now(timezone(timedelta(hours=GMT_OFFSET)))
#         current_time

#         # Slice df5 from order time onward
#         df_after_order = df5.loc[df5.index >= order_dt]

#         if df_after_order.empty:
#             continue

#         highest_high = high.max()
#         lowest_low = low.min()

#         cancel_request = {
#             "action": mt5.TRADE_ACTION_REMOVE,
#             "order": order.ticket,
#             "symbol": symbol,
#         }

#         # 1. BUY LIMIT: trend check
#         # if order_type == mt5.ORDER_TYPE_BUY_LIMIT and not latest_trend.startswith('UpTrend'):
#         #     result = mt5.order_send(cancel_request)
#         #     log_result(result.retcode == mt5.TRADE_RETCODE_DONE,
#         #                f"Cancelled BUY LIMIT (trend change): Order {order.ticket}",
#         #                result.retcode)

#         # 2. SELL LIMIT: trend check
#         # elif order_type == mt5.ORDER_TYPE_SELL_LIMIT and not latest_trend.startswith('DownTrend'):
#         #     result = mt5.order_send(cancel_request)
#         #     log_result(result.retcode == mt5.TRADE_RETCODE_DONE,
#         #                f"Cancelled SELL LIMIT (trend change): Order {order.ticket}",
#         #                result.retcode)

#         # 3. BUY STOP: stop loss hit before activation
#         if order_type == mt5.ORDER_TYPE_BUY_STOP and lowest_low <= sl_price:
#             result = mt5.order_send(cancel_request)
#             log_result(result.retcode == mt5.TRADE_RETCODE_DONE,
#                        f"Cancelled BUY STOP (SL hit before activation): Order {order.ticket}",
#                        result.retcode)

#         # 4. SELL STOP: stop loss hit before activation
#         elif order_type == mt5.ORDER_TYPE_SELL_STOP and highest_high >= sl_price:
#             result = mt5.order_send(cancel_request)
#             log_result(result.retcode == mt5.TRADE_RETCODE_DONE,
#                        f"Cancelled SELL STOP (SL hit before activation): Order {order.ticket}",
#                        result.retcode)