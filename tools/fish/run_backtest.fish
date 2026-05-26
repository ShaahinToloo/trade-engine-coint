#!/usr/bin/env fish

echo "Engine Run"

# safely clear folder
find /home/ubuntu/resources/outputs/data/BackTest/BBMR -mindepth 1 -delete

# run pipeline
./gradlew run
python /home/ubuntu/codes/trade-engine-coint/tools/python/tools/log_monthly.py /home/ubuntu/resources/outputs/data/BackTest/BBMR/1/allTrades.csv
