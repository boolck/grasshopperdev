package com.boolck.dev.listener;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.calc.OrderBookEngine;
import com.boolck.dev.model.OrderBook;
import com.boolck.dev.util.OrderBookRequestFileUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/*
    1. takes input L3 request csv file and parses to  spark DStream that polls for new data every sec
    2. each stream is converts to RDD (immutable resilient distributed dataset)
    3. if there exists a batch RDD from previous call, both RDD are combied and result it send to process BBO
 */
public class SparkUnifiedCSVListener extends AbstractSparkListener {

    private final SparkConf sparkConf;
    private final String dir;
    private  JavaRDD<OrderBook> batchRdd;


    //it takes a directory where a new orderbook file will be pickedup
    public SparkUnifiedCSVListener(String sparkMaster, String dir) {
        this(sparkMaster,dir,null);
    }


    public SparkUnifiedCSVListener(String sparkMaster, String dir, JavaRDD<OrderBook> batchRdd) {
        this.dir = dir;
        sparkConf = new SparkConf().setMaster(sparkMaster).setAppName(this.getClass().getName());
        this.batchRdd = batchRdd;
    }

    //batchRdd is used to unify with streaming RDD and create consolidated input set.
    @Override
    public void process(OrderBookEngine orderBookEngine) throws InputReadException {
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(1));
        //polls directory every 1 sec to look for new files and creates DStream
        JavaDStream<String> msgDataStream = jssc.textFileStream(dir);
        msgDataStream.foreachRDD((VoidFunction<JavaRDD<String>>) rdd -> {
            JavaRDD<OrderBook> streaming_rdd = rdd.map(
                    (Function<String, OrderBook>) OrderBookRequestFileUtil::parseOrderBookRow);
            //unify with batchRDD
            if(batchRdd != null){
                streaming_rdd.union(batchRdd);
            }
            processRDD(sparkConf,streaming_rdd,orderBookEngine);

            //old unifiedRDD becomes new finalBatchRDD
            batchRdd = streaming_rdd;
        });
        //starts streaming
        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
           throw new InputReadException(e.getMessage(),e.getCause());
        }
    }



}