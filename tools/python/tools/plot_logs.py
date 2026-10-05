import numpy as np
import pandas as pd
import mplfinance as mpf
import matplotlib
import matplotlib
matplotlib.use("Qt5Agg")
import matplotlib.pyplot as plt
import os


_BASE = "/root/resources/outputs/data/BBMR/BackTest"
_runs = [
    d
    for d in os.listdir(_BASE)
    if os.path.isdir(os.path.join(_BASE, d)) and d.isdigit()
]
if not _runs:
    raise SystemExit(f"No run folders found under {_BASE}")
CSV_PATH = os.path.join(_BASE, max(_runs, key=int))
print("Plotting:", CSV_PATH)

MAX_POINTS = 4000


def _minmax_downsample(xs, ys):
    n = len(xs)
    if n <= MAX_POINTS:
        return xs, ys
    buckets = MAX_POINTS // 2
    idx = np.minimum(np.arange(n) * buckets // n, buckets - 1)
    out_x, out_y = [], []
    for b in range(buckets):
        sel = idx == b
        if not sel.any():
            continue
        xb, yb = xs[sel], ys[sel]
        i_min = np.argmin(yb)
        i_max = np.argmax(yb)
        for i in sorted({i_min, i_max}):
            out_x.append(xb[i])
            out_y.append(yb[i])
    return np.asarray(out_x), np.asarray(out_y)


def _resample(xs, ys, x0, x1):
    mask = (xs >= x0) & (xs <= x1)
    i0 = np.argmax(mask) if mask.any() else 0
    i1 = len(xs) - np.argmax(mask[::-1]) if mask.any() else 0
    i0 = max(0, i0 - 1)
    i1 = min(len(xs), i1 + 1)
    return _minmax_downsample(xs[i0:i1], ys[i0:i1])


def _cap(xs, ys):
    return _minmax_downsample(np.asarray(xs), np.asarray(ys))


def zoomable_lines(fig, ax, series):
    """series: list of (line, xs, ys) numpy arrays. Redraws only visible points."""
    arrays = [(line, np.asarray(xs), np.asarray(ys)) for line, xs, ys in series]
    updating = [False]

    def on_xlim(ax):
        if updating[0]:
            return
        updating[0] = True
        x0, x1 = ax.get_xlim()
        for line, xs, ys in arrays:
            xv, yv = _resample(xs, ys, x0, x1)
            line.set_data(xv, yv)
        ax.figure.canvas.draw_idle()
        updating[0] = False

    ax.callbacks.connect("xlim_changed", on_xlim)


def _cap(xs, ys):
    step = max(1, len(xs) // MAX_POINTS)
    return np.asarray(xs)[::step], np.asarray(ys)[::step]


def plot_line_zoomable(df, y, **kwargs):
    x0, y0 = _cap(df.index, y)
    (line,) = plt.plot(x0, y0, **kwargs)
    return line, df.index, y


def check_equity():
    df = pd.read_csv(os.path.join(CSV_PATH, "Equity.csv"))
    eq = df["Equity"]

    plt.figure(figsize=(12, 6))
    fig = plt.gcf()
    ax = plt.gca()
    series = [plot_line_zoomable(df, eq, label="Equity", color="black", antialiased=True)]
    zoomable_lines(fig, ax, series)
    # plt.legend()
    plt.show(block=False)


def check_Requity():
    df = pd.read_csv(os.path.join(CSV_PATH, "RealisedEquity.csv"))
    eq = df["RealisedEquity"]

    plt.figure(figsize=(12, 6))
    fig = plt.gcf()
    ax = plt.gca()
    series = [plot_line_zoomable(df, eq, label="RealisedEquity", color="black", antialiased=True)]
    zoomable_lines(fig, ax, series)
    # plt.legend()
    plt.show(block=False)


def check_price():
    df = pd.read_csv(os.path.join(CSV_PATH, "allTrades.csv"))
    df = df.iloc[:]

    price = df["price"]
    priceAsk = df["priceAsk"]
    mavg = df["mavg"]
    std = df["std"]
    first_std = mavg + std
    first_std0 = mavg - std
    second_std = mavg + 2*std
    second_std0 = mavg - 2*std

    tradeType0 = df[df["tradeType"] == 0]
    tradeType1 = df[df["tradeType"] == 1]

    idx = price.index

    plt.figure(figsize=(14, 7))
    fig = plt.gcf()
    ax = plt.gca()
    series = [
        plot_line_zoomable(df, priceAsk, label="PriceAsk", color="green", antialiased=True),
        plot_line_zoomable(df, price, label="PriceBid", color="green", antialiased=True),
        plot_line_zoomable(df, mavg, label="mavgLine", antialiased=True),
        plot_line_zoomable(df, first_std, label="stdLine 1", color="orange", antialiased=True),
        plot_line_zoomable(df, first_std0, label="stdLine 1", color="orange", antialiased=True),
        plot_line_zoomable(df, second_std, label="stdLine 2", color="aqua", antialiased=True),
        plot_line_zoomable(df, second_std0, label="stdLine 2", color="aqua", antialiased=True),
    ]
    zoomable_lines(fig, ax, series)

    plt.scatter(
        tradeType0.index,
        tradeType0["price"],
        color="red",
        label="TradeType 0",
        s=50
    )

    plt.scatter(
        tradeType1.index,
        tradeType1["priceAsk"],
        color="green",
        label="TradeType 1",
        s=50
    )

    closed0 = tradeType0["exitIdx"].dropna().astype(int)
    closed1 = tradeType1["exitIdx"].dropna().astype(int)

    plt.scatter(
        closed0,
        df["priceAsk"].loc[closed0],
        color="black",
        marker="s",
        label="Exit TradeType 0",
        s=50,
        alpha=0.5,
    )
    plt.scatter(
        closed1,
        df["price"].loc[closed1],
        color="black",
        marker="s",
        label="Exit TradeType 1",
        s=50,
        alpha=0.5,
    )

    # plt.legend()
    plt.show(block=True)


if __name__=="__main__":
    check_Requity()
    check_equity()
    check_price()
