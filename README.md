<!-- Add banner here -->

# Project Title
Project DoubleHelix -  Unified Batch & Streaming Order Data Matching Engine

# Table of contents

- [Project Title](#project-title)
- [Table of contents](#table-of-contents)
- [Installation](#installation)
- [Usage](#usage)
- [Design](#design)
- [Enhancement](#enhancement)
- [Release History](#release-history)

# Installation
[(Back to top)](#table-of-contents)

To use this project, first clone the repo on your device using the command below:

```git init```

```git clone https://github.com/boolck/grasshopperdev.git``` 

Verify that repository is checked out & then run

```mvn clean package```

# Usage
[(Back to top)](#table-of-contents)

- A main class ApplicaitonRunner is created to take in input of L43 request file. 
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

Release ver   | Description
------------- | -------------
1.0-SNAPSHOT  | First release. Uses BufferedCSVListener for unified batch and streaming of L3 order requests in csv data interface

