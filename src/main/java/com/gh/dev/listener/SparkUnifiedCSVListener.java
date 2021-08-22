package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.OrderBook;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class SparkUnifiedCSVListener extends AbstractSparkListener {

    private final SparkConf sparkConf;
    private final String dir;
    private JavaRDD<OrderBook> batchRdd;


    //it takes a directory where a new orderbook file will be pickedup
    public SparkUnifiedCSVListener(String sparkMaster, String dir) {
        this(sparkMaster,dir,null);
    }

    //batchRdd is used to unify with streaming RDD and create consolidated input set.
    public SparkUnifiedCSVListener(String sparkMaster, String dir, JavaRDD<OrderBook> batchRdd) {
        this.dir = dir;
        sparkConf = new SparkConf().setMaster(sparkMaster).setAppName(this.getClass().getName());
        this.batchRdd = batchRdd;
    }

    @Override
    public void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, BBOException, InputReadException {
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(1));
        JavaDStream<String> msgDataStream = jssc.textFileStream(dir);
        msgDataStream.foreachRDD(new VoidFunction<JavaRDD<String>>() {
            @Override
            public void call(JavaRDD<String> rdd) throws OrderProcessingException, BBOException, InputReadException {
                JavaRDD<OrderBook> streaming_rdd = rdd.map(new Function<String, OrderBook>() {
                    @Override
                    public OrderBook call(String line) {
                        return OrderBook.parseOrderBookRow(line);
                    }
                });
                //unified
                if(batchRdd!=null){
                    streaming_rdd.union(batchRdd);
                }
                processRDD(sparkConf,streaming_rdd,orderBookEngine);
            }
        });

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
           throw new InputReadException(e.getMessage(),e.getCause());
        }
    }



}