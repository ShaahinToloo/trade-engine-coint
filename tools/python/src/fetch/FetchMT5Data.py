import MetaTrader5 as mt5
import pandas as pd
import numpy as np
from datetime import datetime


class FetchMT5Data:
    def fetch(
        self,
        logger,
        bars=1000,
        symbol="XAUUSD",
        timeframe=mt5.TIMEFRAME_M1,
        start=None,
        end=None,
    ):
        self.logger = logger
        self.symbol = symbol
        self.timeframe = timeframe
        self.bars = bars
        # if bars >= 100000:
        #     self.bars = 99999
        self.start = start
        self.end = end
        return self.__fetch_historical_data()

    def __fetch_historical_data(self) -> pd.DataFrame:
        if not mt5.initialize():
            print("Failed to initialize MetaTrader 5")
            return pd.DataFrame()

        if self.start and self.end:
            start_dt = datetime.strptime(self.start, "%Y-%m-%d %H:%M:%S")
            end_dt = datetime.strptime(self.end, "%Y-%m-%d %H:%M:%S")
            rates = mt5.copy_rates_range(self.symbol, self.timeframe, start_dt, end_dt)
        else:
            print("Failed to get with start and end, fallbacking to bars")
            rates = mt5.copy_rates_from_pos(self.symbol, self.timeframe, 0, self.bars)

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
            self.logger.error("Failed to retrieve OHLC data")
            return pd.DataFrame()

