package com.gh.dev.model;

import com.gh.dev.event.*;
import com.opencsv.bean.CsvBindByPosition;

import java.io.Serializable;
import java.util.Objects;

//input L3OrderBook
public class OrderBook implements Serializable {
    @CsvBindByPosition(position = 0)
    private String seqNum;
    @CsvBindByPosition(position = 1)
    private String addOrderId;
    @CsvBindByPosition(position = 2)
    private String addSide;
    @CsvBindByPosition(position = 3)
    private double addPrice;
    @CsvBindByPosition(position = 4)
    private long addQty;
    @CsvBindByPosition(position = 5)
    private String updateOrderId;
    @CsvBindByPosition(position = 6)
    private String updateSide;
    @CsvBindByPosition(position = 7)
    private double updatePrice;
    @CsvBindByPosition(position = 8)
    private long updateQty;
    @CsvBindByPosition(position = 9)
    private String deleteOrderId;
    @CsvBindByPosition(position = 10)
    private String deleteSide;
    @CsvBindByPosition(position = 11)
    private String tradeOrderId;
    @CsvBindByPosition(position = 12)
    private String tradeSide;
    @CsvBindByPosition(position = 13)
    private double tradePrice;
    @CsvBindByPosition(position = 14)
    private long tradeQty;
    @CsvBindByPosition(position = 15)
    private String time;

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public void setAddOrderId(String addOrderId) {
        this.addOrderId = addOrderId;
    }

    public void setAddSide(String addSide) {
        this.addSide = addSide;
    }

    public void setAddPrice(double addPrice) {
        this.addPrice = addPrice;
    }

    public void setAddQty(long addQty) {
        this.addQty = addQty;
    }

    public void setUpdateOrderId(String updateOrderId) {
        this.updateOrderId = updateOrderId;
    }

    public void setUpdateSide(String updateSide) {
        this.updateSide = updateSide;
    }

    public void setUpdatePrice(double updatePrice) {
        this.updatePrice = updatePrice;
    }

    public void setUpdateQty(long updateQty) {
        this.updateQty = updateQty;
    }

    public void setDeleteOrderId(String deleteOrderId) {
        this.deleteOrderId = deleteOrderId;
    }

    public void setDeleteSide(String deleteSide) {
        this.deleteSide = deleteSide;
    }

    public void setTradeOrderId(String tradeOrderId) {
        this.tradeOrderId = tradeOrderId;
    }

    public void setTradeSide(String tradeSide) {
        this.tradeSide = tradeSide;
    }

    public void setTradeQty(long tradeQty) {
        this.tradeQty = tradeQty;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public  L3Request getL3Request() {
        L3Request request = null;
        if(addOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.addOrderId)
                    .side(Order.Side.valueOf(addSide))
                    .price(addPrice)
                    .qty(addQty)
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request =  new NewOrderRequest(order);
        }
        else if(updateOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.updateOrderId)
                    .side(Order.Side.valueOf(updateSide))
                    .price(updateQty)
                    .qty(updateQty)
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request = new UpdateOrderRequest(order, updatePrice, updateQty);
        }
        else if(deleteOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.deleteOrderId)
                    .side(Order.Side.valueOf(deleteSide))
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request = new CancelOrderRequest(order);
        }
        else if(tradeOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.tradeOrderId)
                    .side(Order.Side.valueOf(tradeSide))
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request= new TradeOrderRequest(order, tradeQty);
        }
        Objects.requireNonNull(request);
        return request;
    }

}
