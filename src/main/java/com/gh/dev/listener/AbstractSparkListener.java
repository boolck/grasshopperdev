package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.event.BBO;
import com.gh.dev.event.L3Request;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.OrderBook;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public abstract class AbstractSparkListener implements SourceListener{

    protected void processRDD(SparkConf sparkConf, JavaRDD<OrderBook> streaming_rdd, OrderBookEngine orderBookEngine) throws OrderProcessingException, BBOException, InputReadException {
        List<L3Request> requestList = new LinkedList<>();
        //TODO parse row in orderbook_rdd to create L3Request);
        orderBookEngine.processRequest(requestList.stream());
        BBO bbo = orderBookEngine.getBbo();
        SparkSession session = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<BBO> dataset = session.createDataset(Collections.singletonList(bbo), Encoders.javaSerialization(BBO.class));
        try {
            StreamingQuery console = dataset.writeStream().outputMode(OutputMode.Complete()).format("console").start();
            console.awaitTermination();
        } catch (TimeoutException | StreamingQueryException e) {
            throw new InputReadException("Error in publishing BBO to stream", e.getCause());
        }
    }
}
