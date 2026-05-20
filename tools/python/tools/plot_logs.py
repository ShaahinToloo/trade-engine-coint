import numpy as np
import pandas as pd
import mplfinance as mpf
import matplotlib
import matplotlib.pyplot as plt
import os


CSV_PATH = str(input("Full Path to desired log folder: "))


def check_equity():
    df = pd.read_csv(os.path.join(CSV_PATH, "/Equity.csv"))
    eq = df["Equity"]

    plt.figure(figsize=(12, 6))
    plt.plot(eq.index, eq, label="Equity", color="black")
    plt.legend()
    plt.show(block=False)


def check_Requity():
    df = pd.read_csv(os.path.join(CSV_PATH, "/RealisedEquity.csv"))
    eq = df["RealisedEquity"]

    plt.figure(figsize=(12, 6))
    plt.plot(eq.index, eq, label="RealisedEquity", color="black")
    plt.legend()
    plt.show(block=False)


def check_price():
    df = pd.read_csv(os.path.join(CSV_PATH, "/Equity.csv"))
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

    plt.figure(figsize=(14, 7))
    plt.plot(price.index, priceAsk, label="PriceAsk", color="green")
    plt.plot(price.index, price, label="PriceBid", color="green")
    plt.plot(price.index, mavg, label="mavgLine")
    plt.plot(price.index, first_std, label="stdLine 1", color="orange")
    plt.plot(price.index, first_std0, label="stdLine 1", color="orange")
    plt.plot(price.index, second_std, label="stdLine 2", color="aqua")
    plt.plot(price.index, second_std0, label="stdLine 2", color="aqua")

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

    plt.legend()
    plt.show(block=True)


if __name__=="__main__":
    check_Requity()
    check_equity()
    check_price()
