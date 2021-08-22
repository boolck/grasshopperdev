package com.gh.dev.event;

import java.io.Serializable;
import java.util.Objects;

public class BBO implements L1Response, Serializable {

    private long seqNum;
    private double bidPrice;
    private long bidQty;
    private double askPrice;
    private long askQty;
    private long timestamp;

    public  BBO(long seqNum,double bidPrice,long bidQty, double askPrice, long askQty, long timestamp){
        this.seqNum = seqNum;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.askPrice = askPrice;
        this.askQty = askQty;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BBO{" +
                "seqNum=" + seqNum +
                ", bidPrice=" + bidPrice +
                ", bidQty=" + bidQty +
                ", askPrice=" + askPrice +
                ", askQty=" + askQty +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BBO bbo = (BBO) o;
        return seqNum == bbo.seqNum &&
                Double.compare(bbo.bidPrice, bidPrice) == 0 &&
                bidQty == bbo.bidQty &&
                Double.compare(bbo.askPrice, askPrice) == 0 &&
                askQty == bbo.askQty &&
                timestamp == bbo.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqNum, bidPrice, bidQty, askPrice, askQty, timestamp);
    }

    public long getSeqNum() {
        return seqNum;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public long getBidQty() {
        return bidQty;
    }

    public double getAskPrice() {
        return askPrice;
    }

    public long getAskQty() {
        return askQty;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
