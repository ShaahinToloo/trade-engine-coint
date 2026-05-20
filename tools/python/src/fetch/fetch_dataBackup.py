import sys

import numpy as np
import pandas as pd

sys.path.insert(0, "/home/void/TradeEngine/app/src/main/java/engine/data/fetch")

import scaler

ohlc_1m = None

logger = None


def get_one_csv():
    global ohlc_1m

    csv_path_1m = (
        "/run/media/void/PC FILES/csv_data/metatrader_XAUUSD_1m_201301_202510.csv"
    )
    ohlc_1m = pd.read_csv(csv_path_1m, parse_dates=["timestamp"], index_col="timestamp")
    ohlc_1m = ohlc_1m.loc[:"2025-06-01 00:00:00"]
    ohlc_1m = ohlc_1m.iloc[-100_000:]

    # Fix the TIMEZONE for Ai TradeFilter Model
    # EET/EETS
    # then convert to UTC
    ohlc_1m.index = pd.to_datetime(ohlc_1m.index)
    ohlc_1m.index = ohlc_1m.index.tz_localize("EET", ambiguous="infer")
    ohlc_1m.index = ohlc_1m.index.tz_convert("UTC")
    ohlc_1m.index = ohlc_1m.index.tz_localize(None)


def get_one_csv_live(bars, symbol):
    global ohlc_1m, logger

    from FetchMT5Data import FetchMT5Data

    gmt5 = FetchMT5Data()
    ohlc_1m = gmt5.fetch(logger=logger, bars=bars, symbol=symbol, timeframe=1)
    ohlc_1m = ohlc_1m.iloc[-50_000:]
    # Fix the TIMEZONE for Ai TradeFilter Model
    # EET/EETS
    # then convert to UTC


def df_scaler(dataframe):
    df = dataframe.copy(deep=True)
    oarr = df["Open"].values.astype(np.float32)[1:]
    harr = df["High"].values.astype(np.float32)[1:]
    larr = df["Low"].values.astype(np.float32)[1:]
    carr = df["Close"].values.astype(np.float32)

    scaled_carr = np.array(
        scaler.scale_volatility(
            carr,
            mean_main=2.6e-5,
            window=5000,
            stride=500,
            high_clip=0.25,
            low_clip=0.15,
        )
    )

    df["Close"] = scaled_carr
    scaled_oarr = df["Close"].shift(1).values.astype(np.float32)
    df["Open"] = scaled_oarr
    df = df.iloc[1:]

    carr = carr[1:]
    scaled_carr = scaled_carr[1:]
    scaled_oarr = scaled_oarr[1:]

    # scaled High and Low
    scaled_harr, scaled_larr = scaler.scale_wicks(
        oarr, harr, larr, carr, scaled_oarr, scaled_carr
    )
    # df['High'] = np.array(scaled_harr)
    df.loc[:, "High"] = np.array(scaled_harr)
    # df['Low'] = np.array(scaled_larr)
    df.loc[:, "Low"] = np.array(scaled_larr)

    return df


def get_scaled_ohlc_mt5(bars, symbol):
    global ohlc_1m

    print(" Fetching Live Data in CSV")
    get_one_csv_live(bars, symbol)

    # print(" Scaling Data")
    # ohlc_1m = df_scaler(ohlc_1m)


def get_scaled_ohlc():
    global ohlc_1m

    print(" Reading CSV")
    get_one_csv()

    # print(" Scaling Data")
    # ohlc_1m = df_scaler(ohlc_1m)


def get_1m_ohlc():
    global ohlc_1m
    if ohlc_1m is None:
        raise ValueError("DataFrame is None")
    cols = ["Open", "High", "Low", "Close", "Volume"]
    return ohlc_1m[cols].to_numpy(dtype=np.float64).T.tolist()


def get_indexes():
    global ohlc_1m
    if ohlc_1m is None:
        raise ValueError("DataFrame is None")

    idx = ohlc_1m.index
    idx = pd.Index(idx).astype(str)
    return [idx.tolist()]
