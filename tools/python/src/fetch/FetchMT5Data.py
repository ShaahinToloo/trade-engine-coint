import MetaTrader5 as mt5
import pandas as pd
import numpy as np
from datetime import datetime


def fetch(bars, symbol):
    # if bars >= 100000:
    #     bars = 99999

    timeframe = mt5.TIMEFRAME_M1
    start = None
    end = None

    return _fetchHistoricalData(
        symbol=symbol, timeframe=timeframe, bars=bars, start=start, end=end
    )


def _fetchHistoricalData(symbol, timeframe, bars, start, end) -> pd.DataFrame:
    for i in range(3):
        if not mt5.initialize():
            print("Failed to initialize MetaTrader 5")
        else:
            break
    else:
        print("Totally Failed to Initialize")

    if start and end:
        start_dt = datetime.strptime(start, "%Y-%m-%d %H:%M:%S")
        end_dt = datetime.strptime(end, "%Y-%m-%d %H:%M:%S")
        rates = mt5.copy_rates_range(symbol, timeframe, start_dt, end_dt)
    else:
        print("Failed to get with start and end, fallbacking to bars")
        rates = mt5.copy_rates_from_pos(symbol, timeframe, 0, bars)

    if rates is not None and len(rates) > 0:
        ohlc_data = pd.DataFrame(rates)
        ohlc_data["time"] = pd.to_datetime(ohlc_data["time"], unit="s")
        ohlc_data.rename(
            columns={
                "time": "timestamp",
                "open": "Open",
                "high": "High",
                "low": "Low",
                "close": "Close",
                "tick_volume": "Volume",
            },
            inplace=True,
        )
        ohlc_data.set_index("timestamp", inplace=True)
        ohlc_data = ohlc_data[["Open", "High", "Low", "Close", "Volume"]]
        return ohlc_data
    else:
        print("Failed to retrieve OHLC data")
        return pd.DataFrame()
