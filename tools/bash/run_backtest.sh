#!/usr/bin/env bash
set -e

echo "Engine Run"

# safely clear folder
find /root/resources/outputs/data/BBMR/BackTest -mindepth 1 -delete

# run pipeline
./gradlew run
python /root/codes/trade-engine-coint/tools/python/tools/log_monthly.py /root/resources/outputs/data/BBMR/BackTest/1/allTrades.csv

rm -r /root/resources/outputs/data/BBMR/BackTest/2
python tools/python/tools/plot_logs.py