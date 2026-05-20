import pandas as pd
import sys


CSV_NAME = str(sys.argv[1])
DATE_NAME = str(sys.argv[2])
TIME_NAME = str(sys.argv[3])


df = pd.read_csv(CSV_NAME)
df.reset_index(inplace=True)


df["timestamp"] = df[DATE_NAME] + " " + df[TIME_NAME]
df["timestamp"] = df["timestamp"].str.replace(".", "-")
df["timestamp"] = df["timestamp"] + ":00"

df = df.drop(columns=["index", DATE_NAME, TIME_NAME], error='ignore')
df = df.set_index("timestamp")
df.index = pd.to_datetime(df.index)
df = df.sort_index()


CSV_NAME = CSV_NAME.replace(".csv", "_FixedIdx.csv")
df.to_csv(CSV_NAME)
