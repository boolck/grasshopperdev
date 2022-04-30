package com.boolck.dev.listener;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.calc.OrderBookEngine;
import com.boolck.dev.model.BBO;
import com.boolck.dev.event.L3Request;
import com.boolck.dev.model.OrderBook;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/*
    1. takes input of stream of  OrderBook RDD (immutable resilient distributed dataset) and converts to L3 request stream
    2. l3 request stream is passed to order book engine to create BBO
    3. full  BBO is fetched and serialized to Spark dataset (equivalent of table)
    4. BBO dataset is streamed back to o/p write stream
 */
public abstract class AbstractSparkListener implements SourceListener{

    protected void processRDD(SparkConf sparkConf, JavaRDD<OrderBook> streaming_rdd, OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException {
        List<L3Request> requestList = streaming_rdd.collect().stream().map(OrderBook::getL3Request).collect(Collectors.toList());
        orderBookEngine.processRequest(requestList.stream());
        List<BBO> bbo = orderBookEngine.getBBOList();
        SparkSession session = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<BBO> dataset = session.createDataset(bbo, Encoders.javaSerialization(BBO.class));
        try {
            StreamingQuery console = dataset.writeStream().outputMode(OutputMode.Complete()).format("console").start();
            console.awaitTermination();
        } catch (TimeoutException | StreamingQueryException e) {
            throw new InputReadException("Error in publishing BBO to stream", e.getCause());
        }
    }
}
