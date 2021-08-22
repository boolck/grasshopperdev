package com.gh.dev.model;

import java.util.Objects;

public class Order {
    //TODO replace long with BigInteger
    private final long seqNum;
    private final String orderId;
    private final Side side;
    //TODO replace double with BigDecimal
    private double price;
    private long qty;
    private final long timestamp; // time in UTC msec since epoch

    private Order(OrderBuilder builder) {
        this.seqNum = builder.seqNum;
        this.orderId = builder.orderId;
        this.side = builder.side;
        this.price = builder.price;
        this.qty = builder.qty;
        this.timestamp = builder.timestamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        this.qty = qty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSeqNum() {
        return seqNum;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.orderId, this.side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId) &&
                side == order.side;
    }

    @Override
    public String toString() {
        return "Order{" +
                "seqNum=" + seqNum +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", timestamp=" + timestamp +
                '}';
    }

    public boolean isBuy() {
        return Side.BUY==this.side;

    }

    public static class OrderBuilder {
        private long seqNum;
        private String orderId;
        private Side side;
        private double price;
        private long qty;
        private long timestamp;

        public OrderBuilder seqNum(long seqNum) {
            this.seqNum = seqNum;
            return this;
        }

        public OrderBuilder id(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderBuilder side(Side side) {
            this.side = side;
            return this;
        }

        public OrderBuilder price(double price) {
            this.price = price;
            return this;
        }

        public OrderBuilder qty(long qty) {
            this.qty = qty;
            return this;
        }

        public OrderBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Order build() {
            validateOrder();
            return new Order(this);
        }

        private void validateOrder() {
            Objects.requireNonNull(this.orderId);
            Objects.requireNonNull(this.side);
        }
    }

    public enum Side {
        BUY,
        SELL
    }
}
