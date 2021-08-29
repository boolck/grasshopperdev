package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.OrderBook;
import com.gh.dev.util.OrderBookRequestFileUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;

/*
    1. takes input L3 request csv file and parses to  OrderBook RDD (immutable resilient distributed dataset)
    2. orderbook RDD is passed to abstract spark listener for further processing
    3. works in batch mode as full csv is parsed
 */
public class SparkBatchCSVListener extends AbstractSparkListener {
    private final SparkConf sparkConf;
    private final String file;
    private JavaRDD<OrderBook> orderBookRdd;

    public SparkBatchCSVListener(String sparkMaster, String file) {
        this.file = file;
        sparkConf = new SparkConf().setMaster(sparkMaster).setAppName(this.getClass().getName());

    }

    @Override
    public void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException {
        this.orderBookRdd = getOrderBookRDD();

        super.processRDD(sparkConf, orderBookRdd, orderBookEngine);
    }

    protected JavaRDD<OrderBook> getOrderBookRDD() {
        if (this.orderBookRdd != null) {
            return this.orderBookRdd;
        }
        SparkSession session = SparkSession.builder().config(sparkConf).getOrCreate();
        this.orderBookRdd = session.read()
                .textFile(file)
                .javaRDD()
                .map((Function<String, OrderBook>) OrderBookRequestFileUtil::parseOrderBookRow);

        return this.orderBookRdd;
    }

}
