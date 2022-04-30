<!-- Add banner here -->

# Project Title
Project Double Helix - A Unified Batch & Streaming L3 to L1 market data state machine

# Table of contents

- [Project Title](#project-title)
- [Table of contents](#table-of-contents)
- [Description](#description)
- [Installation](#installation)
- [Usage](#usage)
- [Design](#design)
- [Enhancement](#enhancement)
- [Release History](#release-history)

# Description
The purpose of this task is to convert Level 3 market data to Level 1 market data. 

Input:

An order book of Level 3 (L3) data is provided that indicates the state of the market at a point in time. This shows for each price level and each side (buy/sell) the orders for that level.
Because sending the full state of the order book for each modification to the book is slow and wastes bandwidth, the exchange publishes deltas/diffs instead, which describes updates to the state of the book.
Each order has a side, price and quantity. They are uniquely identified by an order id. An order update can be one of:
● ADD inserts a new order into the book.
● UPDATE modifies an existing order on the book.
● DELETE removes the order from the book.
● TRADE matches buy orders with sell orders at a specific price; the corresponding update to the book being a reduction of quantity outstanding in the respective orders according to the amount traded

Output:

The task creates BBO -(best bid and offer) that constitutes the lowest sell price level (ask price) with all the cumulative quantity at ask price level (ask size) and the highest buy price level (bid price) with all the cumulative quantity at both levels.
Level 1 (L1) data refers to the stream of updates to BBO as a consequence of updates to the book state. It is derived data, with the L3 data being the raw data. Unlike with L3 data the full state of the BBO should be published each time as it is not a voluminous amount of data.




# Installation
[(Back to top)](#table-of-contents)

To use this project, first clone the repo on your device using the command below:

```git init```

```git clone https://github.com/boolck/doublehelix.git``` 

Verify that repository is checked out & then run

```mvn clean package```

# Usage
[(Back to top)](#table-of-contents)

- A main class ApplicationRunner is created to take in input of L43 request file. 
- If no file is provided, default input l3_data_v3.csv is used.

To call the main ApplicationRunner class:

```mvn clean compile exec:java```

or explicitly call it by passing argument

```mvn clean compile exec:java -Dexec.mainClass="com.gh.dev.ApplicationRunner"```

The test setup that uses provided L3 & 11 csv can be run by 

```mvn clean test```

# Design
[(Back to top)](#table-of-contents)

There are 2 main actors of the program
- Input Request parser - 
    - BufferedCSVListener has both batch and streaming processing support. 
    - A batch size is specified for processing request in bulk and a batch size of unity translates the processing to streaming
    - In future release, we can replace that with Spark Streaming processor (source code stubbed in SparkUnifiedCSVListener.java)
    - A unified data processing engine is simulated in test case TestBufferedCSVLister->testUnifiedL3ProcessRequest
-  Calculator for generating BBO 
    - OrderBookEngine has all the functional processing logic to process requests from L3 csv listener
    - It maintains 2 priority queues - one for bid and other for ask and keeps updating them as new request comes
    - It checks if the order is out of sequence, then it puts in buffer. after each request, buffer is reattempted to be processed
    - If the files reaches the end or count of out of order messages reaches threshold, buffer is processed & flushed

Additionally we generate OrderBookAnalytics to get 
- initial sequence number (of the zeroth row), same is passed as seed to OrderBookingEngine
- maximum gap in sequence number (used as BigInteger) between two consecutive requests, this is threashold of processing buffer

# Enhancement
[(Back to top)](#table-of-contents)

Enhancements for future releases:
- Setup test bed for using Spark Streaming (source code implemented in SparkUnifiedCSVListener.java)
- Implement wait time based out of sequence messages processing (currently its window based i.e. max gap of seqnumber in request file) 
- Abstract multiple sinks for BBO so that in future it's persisted to file/ DB/ message bus. (currently it's printed to console)

# Release History
[(Back to top)](#table-of-contents)

| Release ver  | Description                                                                                                        |
|--------------|--------------------------------------------------------------------------------------------------------------------|
| 1.0-SNAPSHOT | First release. Uses BufferedCSVListener for unified batch and streaming of L3 order requests in csv data interface |

