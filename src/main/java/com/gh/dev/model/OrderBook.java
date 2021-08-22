package com.gh.dev.model;

import java.io.Serializable;

public class OrderBook implements Serializable {
    private long seq_num;
    private String add_order_id;
    private String add_side;
    private double add_price;
    private long add_qty;
    private String update_order_id;
    private String update_side;
    private double update_price;
    private long update_qty;
    private String delete_order_id;
    private String delete_side;
    private String trade_order_id;
    private String trade_side;
    private long trade_qty;
    private double trade_price;
    private long time;

    public long getSeq_num() {
        return seq_num;
    }

    public void setSeq_num(long seq_num) {
        this.seq_num = seq_num;
    }

    public String getAdd_order_id() {
        return add_order_id;
    }

    public void setAdd_order_id(String add_order_id) {
        this.add_order_id = add_order_id;
    }

    public String getAdd_side() {
        return add_side;
    }

    public void setAdd_side(String add_side) {
        this.add_side = add_side;
    }

    public double getAdd_price() {
        return add_price;
    }

    public void setAdd_price(double add_price) {
        this.add_price = add_price;
    }

    public long getAdd_qty() {
        return add_qty;
    }

    public void setAdd_qty(long add_qty) {
        this.add_qty = add_qty;
    }

    public String getUpdate_order_id() {
        return update_order_id;
    }

    public void setUpdate_order_id(String update_order_id) {
        this.update_order_id = update_order_id;
    }

    public String getUpdate_side() {
        return update_side;
    }

    public void setUpdate_side(String update_side) {
        this.update_side = update_side;
    }

    public double getUpdate_price() {
        return update_price;
    }

    public void setUpdate_price(double update_price) {
        this.update_price = update_price;
    }

    public long getUpdate_qty() {
        return update_qty;
    }

    public void setUpdate_qty(long update_qty) {
        this.update_qty = update_qty;
    }

    public String getDelete_order_id() {
        return delete_order_id;
    }

    public void setDelete_order_id(String delete_order_id) {
        this.delete_order_id = delete_order_id;
    }

    public String getDelete_side() {
        return delete_side;
    }

    public void setDelete_side(String delete_side) {
        this.delete_side = delete_side;
    }

    public String getTrade_order_id() {
        return trade_order_id;
    }

    public void setTrade_order_id(String trade_order_id) {
        this.trade_order_id = trade_order_id;
    }

    public String getTrade_side() {
        return trade_side;
    }

    public void setTrade_side(String trade_side) {
        this.trade_side = trade_side;
    }

    public long getTrade_qty() {
        return trade_qty;
    }

    public void setTrade_qty(long trade_qty) {
        this.trade_qty = trade_qty;
    }

    public double getTrade_price() {
        return trade_price;
    }

    public void setTrade_price(double trade_price) {
        this.trade_price = trade_price;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public static OrderBook parseOrderBookRow(String line) {
        String[] parts = line.split(",");
        OrderBook orderBook = new OrderBook();
        int index = 0;
        orderBook.setSeq_num(Long.parseLong(parts[index++]));
        orderBook.setAdd_order_id(parts[index++]);
        orderBook.setAdd_side(parts[index++]);
        orderBook.setAdd_price(Double.parseDouble(parts[index++]));
        orderBook.setAdd_qty(Long.parseLong(parts[index++]));
        orderBook.setUpdate_order_id(parts[index++]);
        orderBook.setUpdate_side(parts[index++]);
        orderBook.setUpdate_price(Double.parseDouble(parts[index++]));
        orderBook.setUpdate_qty(Long.parseLong(parts[index++]));
        orderBook.setDelete_order_id(parts[index++]);
        orderBook.setDelete_side(parts[index++]);
        orderBook.setTrade_order_id(parts[index++]);
        orderBook.setTrade_side(parts[index++]);
        orderBook.setTrade_price(Double.parseDouble(parts[index++]));
        orderBook.setTrade_qty(Long.parseLong(parts[index++]));
        orderBook.setTime(Long.parseLong(parts[index++]));
        return orderBook;
    }

}
