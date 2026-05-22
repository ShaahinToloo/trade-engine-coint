# Trade Engine Cointegration System

<!-- Whats left: 
    Backtest class must be abstracted.
    Evey sys PATH must be set in PublicConstants.java, not in classes like Python.java, etc.

    Note for myself: Add these ASAP
    - some features commented in featureUtils and data.feature
-->

A modular quantitative trading engine focused on **Low Latency** and **Ease Of Use**.

This project is designed as a **Light, Modular, Fast framework**. Combining Java-based engine infrastructure with Python-based data and order handling, and analysis tooling.

---

## 📌 Project Status

This project is currently in active development.

- [x] **Backtesting layer:** working and validated
- [ ] **Live execution layer:** under development
- [ ] **Documentation:** more official docstrings and inline docs will be added soon

The project structure below shows how the system is organized and how the different layers fit together.

---

## ⚙️ Core Idea

The system is built around:

- A fast backtesting and live-execution abstraction layer
- Feature caching for speed and time-series engineering
- Python tooling for data pipelines and analysis
- A cointegration-based core strategy, with room to implement your own strategy by adjusting the abstract layers
- A design focused on speed, extensibility, and readability

This project is primarily built around cointegration and Johansen-based logic, but it is not limited to that. You can adapt it to run your own strategy with some changes to the abstract classes.

The `Core` layer is designed to handle the full workflow internally. It only needs to be called on new price arrival, and it automatically handles:

- Feature updates
- Strategy execution
- Trade entry generation
- Trade close generation

On my machine (`i7-2600K`), the average `Core` execution time is around **60 microseconds**.

The current implementation is already fast enough for second-frequency trading. The main bottleneck is MT5 integration, which can later be replaced with a direct Java API implementation for lower overhead and a fully Java-native execution path.

> [!TIP]
> Performance can still be significantly improved by caching:
> - Johansen test calculations
> - Lookback calculations
> 
> The current implementation already uses simple array caching approaches such as `System.arraycopy()` (`O(n)`), while future optimizations may move toward ring-buffer based structures with `O(1)` update complexity.

---

## 🧠 Architecture Overview

The project is split into two main layers:

### 1. Java Engine (`app/src/main/java/engine`)
Responsible for:
- Backtest & Execution Heads
- Core execution engine
- Strategy logic
- Risk management
- Trade lifecycle management
- Metrics + reporting
- Mathematical utilities (matrix, decomposition, time-series ops)

### 2. Python Tooling (`tools/python`)
Responsible for:
- Data ingestion (MT5, CSV loaders)
- Feature engineering
- Research scripts
- Logging + visualization
- Execution helpers bridging external systems

---

## 🧩 Engine Components (Java)

### Cointegration Utilities
- Johansen test
- Half-life estimation
- Synthetic portfolio construction

### System Core
- Core workflow orchestration
- Feature updates on new price arrival
- Strategy evaluation and trade signal output

### Backtest Engine
- Backtest execution flow
- State tracking
- Trade lifecycle handling

### Strategy Logic
- Implemented strategies
- Strategy abstraction layer
- Strategy-specific decision making

### Data Layer (Python)
- Data fetching and loading
- Feature engineering
- Feature caching

### Math Utilities
- Matrix and vector operations
- Eigen / Cholesky decomposition
- Normalization
- Time-series transformations

### Metrics
- Performance metrics
- Risk metrics
- Trade post-calculation and tracker metrics

### Logging
- File-based logging
- Data persistence logs

### Reporters
- Progress reporting
- Backtest result reporting

### Execution Layer
- Order gateway abstraction
- Risk state management
- Watchdog protection
- State snapshot storage

### Time Utilities
- Market session handling
- Sleep and timing helpers

### Trade Management
- Trade data handling
- Trade manager utilities

### Tuning
- Hyperparameter tuning for constant parameters

---

## 🐍 Python Tools

Located in:

***tools/python/***

Includes:
- Market data loaders (MT5, CSV)
- News fetching file
- Order submit file
- Analysis scripts

---

## 🧠 Design Philosophy

- Modular layers
- Separation of research vs production logic
- Statistical rigor over heuristics
- Reproducible backtesting pipelines
- Extensibility for future strategies and self-development

---

## 📁 Project Structure

Note: resource/ folders are not included in this tree.

    app/src
    ├── main
    │   └── java/engine
    │       ├── cointegration                  → Anything related to Co-Integration
    │       │   ├── halflife
    │       │   │   ├── Halflife.java                      → Static
    │       │   │   └── LookbackCalculator.java            → Static
    │       │   │
    │       │   ├── johansen
    │       │   │   ├── JohansenResult.java
    │       │   │   └── JohansenTest.java                  → Static
    │       │   │
    │       │   └── portfolio
    │       │       └── SyntheticPortfolio.java            → Static
    │       │
    │       ├── constants                      → Project parameters are set here
    │       │   ├── BacktestConstants.java                 → Static
    │       │   ├── ExecutionConstants.java                → Static
    │       │   └── PublicConstants.java                   → Static
    │       │
    │       ├── core                           → Handles corresponding Logic (strategy)
    │       │   ├── BBCore.java
    │       │   └── Core.java                              → Abstract
    │       │
    │       ├── data                           → Anything related to data
    │       │   ├── feature                    → Anything related to feature engineering and keeping features updated on each price
    │       │   │   ├── CachedFeatureInitializer.java
    │       │   │   ├── CachingFeatureDerivator.java
    │       │   │   └── CachingMainFeatures.java
    │       │   │
    │       │   └── fetch                      → Anything related to loading and fetching
    │       │       └── BacktestDataLoader.java
    │       │
    │       ├── exceptions                     → Custom exceptions
    │       │   ├── BrokenCodeException.java
    │       │   └── RecoverableException.java
    │       │
    │       ├── execution                      → Live trading execution utilities: risk, order routing, and logging
    │       │   ├── OrderGateway.java
    │       │   ├── Python.java                            → Uses tools/python/src/execution/pythonMethods.py
    │       │   ├── RiskState.java
    │       │   ├── StateSnapshotStore.java
    │       │   └── WatchDog.java
    │       │
    │       ├── featureUtils                   → String Key generation for features
    │       │   ├── KeyGen.java                            → Static
    │       │   ├── KeyValDerived.java                     → Static
    │       │   └── KeyValMain.java                        → Static
    │       │
    │       ├── heads                          → Handles corresponding Core
    │       │   ├── BBExecutionEngine.java
    │       │   ├── Backtest.java
    │       │   ├── ExecutionEngine.java                   → Abstract
    │       │   └── HeadState.java                         → Struct
    │       │
    │       ├── logger                         → Handles IO. They do not print anything to the terminal
    │       │   ├── DataLogger.java
    │       │   ├── InfoLogger.java
    │       │   └── LoggerUtils.java
    │       │
    │       ├── main                           → Main classes containing the Java main method
    │       │   ├── BBMain.java
    │       │   └── Main.java                              → Abstract
    │       │
    │       ├── mathUtils                      → All math utilities
    │       │   ├── algebra                                → Empty
    │       │   │
    │       │   ├── decomposition
    │       │   │   ├── choleskyUtils
    │       │   │   │   └── Cholesky.java                  → Static
    │       │   │   │
    │       │   │   └── eigenUtils
    │       │   │       └── Eigen.java                     → Static
    │       │   │
    │       │   ├── matrix
    │       │   │   ├── MatrixArithmetic.java              → Static
    │       │   │   ├── MatrixDivision.java                → Static
    │       │   │   ├── MatrixMultiplication.java          → Static
    │       │   │   ├── MatrixOps.java                     → Static
    │       │   │   └── MatrixTransform.java               → Static
    │       │   │
    │       │   ├── normalization
    │       │   │   ├── MatrixNormalization.java           → Empty
    │       │   │   ├── ScalarNormalization.java           → Empty
    │       │   │   └── VectorNormalization.java           → Static
    │       │   │
    │       │   ├── stats
    │       │   │   └── JohansenCriticalValues.java        → Static
    │       │   │
    │       │   ├── timeseries
    │       │   │   ├── Detrending.java                    → Static
    │       │   │   └── Differencing.java                  → Static
    │       │   │
    │       │   └── vector
    │       │       ├── VectorOps.java                     → Static
    │       │       └── VectorSorting.java                 → Static
    │       │
    │       ├── metrics                        → Strategy metrics
    │       │   ├── PerformanceMetrics.java
    │       │   ├── RiskMetrics.java
    │       │   └── TradeMetrics.java
    │       │
    │       ├── reporter                       → Reporters are not Loggers. They only print in terminal
    │       │   ├── BacktestReporter.java                  → Its output is passed to DataLogger for I/O logging
    │       │   └── ProgressReporter.java
    │       │
    │       ├── state                          → Anything related to tracking during Backtest 
    │       │   ├── BalanceTracker.java
    │       │   └── EquityTracker.java
    │       │
    │       ├── strategyLogic                  → Implemented strategies
    │       │   ├── BBLogic.java
    │       │   └── Logic.java                             → Abstract
    │       │
    │       ├── timeUtils                      → Time utilities for live trading and execution
    │       │   ├── MarketSession.java                     → Static
    │       │   ├── SleeperUtils.java                      → Static
    │       │   └── TimeUtils.java                         → Static
    │       │
    │       ├── trade                          → Anything related to managing trades
    │       │   ├── Trade.java                             → Struct
    │       │   └── TradeManager.java                      → Static
    │       │
    │       └── tuner                          → Tunes constant parameters
    │           └── HyperparameterTuner.java
    │
    ├── test
    │   └── java/engine
    │       ├── cointegration
    │       │   ├── halflife
    │       │   │   └── LookbackCalculatorTest.java
    │       │   │
    │       │   └── johansen
    │       │       └── JohansenTestTest.java
    │       │
    │       └── mathUtils
    │           ├── decomposition
    │           │   ├── choleskyUtils
    │           │   │   └── CholeskyTest.java
    │           │   │
    │           │   └── eigenUtils
    │           │       └── EigenTest.java
    │           │
    │           ├── matrix
    │           │   ├── MatrixDivisionTest.java
    │           │   ├── MatrixMultiplicationTest.java
    │           │   ├── MatrixOpsTest.java
    │           │   └── MatrixTransformTest.java
    │           │
    │           ├── timeseries
    │           │   ├── DetrendingTest.java
    │           │   └── DifferencingTest.java
    │           │
    │           └── vector
    │               └── VectorSortingTest.java
    │
    └── tools
        ├── fish
        │   └── run_backtest.fish
        │
        └── python
            ├── pyproject.toml
            ├── ruff.toml
            ├── src
            │   ├── execution
            │   │   └── pythonMethods.py
            │   │
            │   └── fetch
            │       ├── FetchMT5Data.py
            │       ├── TimeFeatures.py
            │       └── load_csv_data.py
            │
            └── tools                          → Scripts for post-backtest analysis
                ├── concat_date_time.py
                ├── log_monthly.py
                └── plot_logs.py

---

## 🧰 Requirements

### Java
- Java 21+ recommended. You can obviously use lower too.
- Gradle wrapper included

### Python

- Python version 3.10.11 is recommended
- Install dependencies:

#### 🧪 Development environment

Used for research, testing, plotting, and experimentation (appliable to any OS):
```bash
pip install -r py_requirements_dev.txt
```

#### 🚀 Execution environment

Used for live execution and production runs (Only appliable to Windows):
```bash
pip install -r py_requirements_execution.txt
```

#### 🔎 What's the difference between dev and execution py reqs?
execution contains more dependencies for live trading:
 - MetaTrader5 dependency for data fetching and order execution
 - Web dependencies for news fetching

>[!WARNING]
>**⚠️ Important note on MetaTrader5:**
>
>MetaTrader5 only installs DLL dependencies by now, so its only installable in Windows environment.\
>Also you may want to use Wine, KVM, QEMU or any other windows VM, hence I personally don't suggest that due to their non-stability.

---

## ⚠️ Disclaimer

*Note that No financial advice is provided.*\
This project is for **Saving Time** and your **Nerves**.

---

## 🚀 Running

### 🖥️ Just do

```bash
./gradlew run
```

This will build first, then runs. If you just need build do `build` instead of `run`.

### *It doesn't work? Well, It shouldn't work! See below:*

You must set these paths:
- Set python's jep (v4.2.2) in build.gradle.kts file.
- Set paths in engine.constants.PublicConstants.java

If you set these you are all set. But in case if you got some other path problems in python files or jep invoking, just hardcode paths from your root (X:\\ or /), Don't do ~

> [!TIP]
> If you keep getting jep problems, you should use a python venv

---

## 📐 Workflow & Wiring

Read this part if you are curious about the project or you want to use it.

This project's parts are very modular, means that you can easily swap strategies, tune hyper-params and switch from backtest to execution.\
Through the workflow, we have 3 main parts. These parts are logic-based, not data-based codes.\

### Head <-> Core <-> Logic

#### Head

- **What is a Head?**
  - Head is basically the upper layer of the project.
  - Head calls a Core, it Must not call a Logic.

- Head handles the HeadState, logging objects, and your custom functionalities.
- HeadState is a struct class in the heads package. It's responsible for holding tracking metrics, post metrics and sliced auto-updatable data arrays.

- *Examples:*
  - Parent: Backtest.java
  - Child : BBBacktest.java

  - Parent: ExecutionEngine.java
  - Child : BBExecutionEngine.java

#### Core

- **What is a Core?**
  - Core is basically the middle layer of the project.
  - Core gets called by a Head & Core calls a Logic.

- Gets the new price on each processCandle()
- Outs void for abstraction. But you must fetch it's internal TradeContext Class fields:
  - Core.TradeContext.trade -> (no new trades) If (trade is null) else (there is a new trade)
  - Core.TradeContext.tradeIndicesToEliminate ->
    - Length equals to your TradeManager.openTrades list
    - This is a binary array, contains only 0 & 1
    - If at index **x**, the value is 0. Means that this trade must still be open
    - Else If at index **x**, the value is 1. Means that this trade must be closed NOW!

- *Examples:*
  - Parent: Core.java
  - Child : BBCore.java

#### Logic

- **What is a Logic?**
  - It's the trading strategy (Handles Entry and Exit)
  - Logic is basically the lower layer of the project.
  - Logic gets called by a Core.

- Logic is a dumb class. Means that it doesn't know anything about the global data arrays.
- So the Portfolio Matrix must be passed to it on each call via reValue()

- *Examples:*
  - Parent: Logic.java
  - Child : BBLogic.java

---

## 🔨 Feature Engineering

Currently, the feature engineering parts work correctly.\
There are no issues, but it can be hard to implement new features right now. I'm currently working on execution and feature engineening too to make it easy to use.

If you add a feature to FE classes, they can be accessed via PublicConstants class global Maps.\
There are some features commented out in data.feature package classes, just uncomment them to use them.

To add new features:
- Add its key in package featureUtils
- Add itself in package data.feature

classes with KeyWord **Main** contains the main features derived from price.\
eg: ATR, SMA, RSI, ...

classes with KeyWord **Derived** contains the drived features of the main features.\
eg: slope, pctChange, absolute, ...

---

## 👤 Author

**ME!** Which is basically Shaahin Toloo.

Quantitative developer + systems engineering focus

---

## 📜 License

### MPL 2.0

See LICENSE file.

---
