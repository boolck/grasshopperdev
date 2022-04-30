package com.boolck.dev.util;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.model.OrderBook;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


public  class OrderBookRequestFileUtil {

    public static class OrderBookAnalytics{
        private final int maxSequenceNumberGap;
        private final String earliestSeqNum;
        private final List<OrderBook> orderBookList;

        public OrderBookAnalytics(String fileName,int maxSequenceNumberGap, String earliestSeqNum,List<OrderBook> orderBookList){
            this.maxSequenceNumberGap = maxSequenceNumberGap;
            this.earliestSeqNum = earliestSeqNum;
            this.orderBookList = orderBookList;
        }

        public int getMaxSequenceNumberGap() {
            return maxSequenceNumberGap;
        }

        public String getEarliestSeqNum() {
            return earliestSeqNum;
        }

        public List<OrderBook> getOrderBookList(){
            return orderBookList;
        }
    }

    /*
        parses the L3 request csv and gathers data points useful for making assumptions
        it reads all seqnumber and finds the earliest seqNumber for order engine to initialize with.
        that initial number is used later in comparison.
        Also gets the maximum difference in seq number which is a threshold of buffer in order processing engine.
        if buffer reaches that maxwindow, messages are processed.
     */
    public static OrderBookAnalytics parseRequestFile(String l3RequestFile) throws InputReadException {
        Path filePath = Path.of(l3RequestFile);
        try {
            BufferedReader l1Reader = new BufferedReader(
                    new FileReader(filePath.toFile().getAbsolutePath()));
            l1Reader.readLine();
            CsvToBean<OrderBook> csvReader = new CsvToBeanBuilder<OrderBook>(l1Reader)
                    .withType(OrderBook.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            List<OrderBook> allL3requests = csvReader.parse();
            List<OrderBook> sortedL3Request = allL3requests.
                    stream().
                    sorted((b1,b2)-> OrderBookRequestFileUtil.compareSeqNum(b1.getSeqNum(),b2.getSeqNum())).
                    collect(Collectors.toList());

            String earliestSeqNum = sortedL3Request.get(0).getSeqNum();

            int maxSequenceNumberGap = Integer.MIN_VALUE;
            int bound = sortedL3Request.size() - 1;
            for (int index = 0; index < bound; index++) {
                String nextSeqNum = sortedL3Request.get(index + 1).getSeqNum();
                String thisSeqNum = sortedL3Request.get(index).getSeqNum();
                maxSequenceNumberGap = Math.max(
                        maxSequenceNumberGap,
                        OrderBookRequestFileUtil.compareSeqNum(nextSeqNum, thisSeqNum));
            }

            return new OrderBookAnalytics(l3RequestFile,maxSequenceNumberGap,earliestSeqNum,allL3requests);

        } catch (IOException e) {
            throw new InputReadException("Error loading file "+l3RequestFile,e);
        }
    }

    public static int compareSeqNum(String thisSeqNum, String otherSeqNum){
        BigInteger thisSeqNumAsInt = new BigDecimal(thisSeqNum).toBigIntegerExact();
        BigInteger otherSeqNumAsInt = new BigDecimal(otherSeqNum).toBigIntegerExact();
        return thisSeqNumAsInt.subtract(otherSeqNumAsInt).intValue();
    }

    public static OrderBook parseOrderBookRow(String line) {
        String[] parts = line.split(",");
        OrderBook orderBook = new OrderBook();
        orderBook.setSeqNum(parts[0]);
        if(!parts[1].isEmpty()){
            orderBook.setAddOrderId(parts[1]);
            orderBook.setAddSide(parts[2]);
            orderBook.setAddPrice(Double.parseDouble(parts[3]));
            orderBook.setAddQty(Long.parseLong(parts[4]));
        }
        else if(!parts[5].isEmpty()){
            orderBook.setUpdateOrderId(parts[5]);
            orderBook.setUpdateSide(parts[6]);
            orderBook.setUpdatePrice(Double.parseDouble(parts[7]));
            orderBook.setUpdateQty(Long.parseLong(parts[8]));
        }
        else if(!parts[9].isEmpty()){
            orderBook.setDeleteOrderId(parts[9]);
            orderBook.setDeleteSide(parts[10]);
        }
        else if(!parts[11].isEmpty()){
            orderBook.setTradeOrderId(parts[11]);
            orderBook.setTradeSide(parts[12]);
            orderBook.setTradePrice(Double.parseDouble(parts[13]));
            orderBook.setTradeQty(Long.parseLong(parts[14]));
        }
        orderBook.setTime(parts[15]);
        return orderBook;
    }

}
