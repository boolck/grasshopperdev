package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.OrderBook;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;

public class SparkBatchCSVListener extends AbstractSparkListener {
    private final SparkConf sparkConf;
    private final String file;
    private JavaRDD<OrderBook> orderBookRdd;

    public SparkBatchCSVListener(String sparkMaster, String file) {
        this.file = file;
        sparkConf = new SparkConf().setMaster(sparkMaster).setAppName(this.getClass().getName());

    }

    @Override
    public void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, BBOException, InputReadException {
        SparkSession session = SparkSession.builder().config(sparkConf).getOrCreate();

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
                .map(new Function<String, OrderBook>() {
                    @Override
                    public OrderBook call(String line) {
                        return OrderBook.parseOrderBookRow(line);
                    }
                });

        return this.orderBookRdd;
    }

}
