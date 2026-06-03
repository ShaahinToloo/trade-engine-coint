import pandas as pd
import os
import sys


if len(sys.argv) >= 2:
    CSV_PATH = sys.argv[1]
else:
    CSV_PATH = input("Full Path to allTrades.csv: ")

df = pd.read_csv(CSV_PATH)
df["timestamp"] = pd.to_datetime(df["timestamp"])

# keep only rows where a trade actually closes
df = df.dropna(subset=["pnlOnExit"]).copy()

# ensure numeric
df["pnlOnExit"] = pd.to_numeric(df["pnlOnExit"], errors="coerce")
df = df.dropna(subset=["pnlOnExit"])

# monthly grouping
df["month"] = df["timestamp"].dt.to_period("M")

results = []

for month, group in df.groupby("month"):
    group = group.sort_values("timestamp")

    cumulative_pnl = group["pnlOnExit"].cumsum().values
    net_pnl = cumulative_pnl[-1] if len(cumulative_pnl) > 0 else 0.0

    results.append({
        "month": str(month),
        "trades": len(group),
        "net_pnl": net_pnl,
        "avg_pnl": group["pnlOnExit"].mean(),
        "max_drawdown_in_month": (cumulative_pnl - pd.Series(cumulative_pnl).cummax()).min()
    })

stats = pd.DataFrame(results)

print(stats)
