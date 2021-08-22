package com.gh.dev.event;


import com.gh.dev.model.Order;

public interface L3Request extends Comparable {
    public enum RequestType{
        NEW,UPDATE,CANCEL,TRADE;
    }

    public RequestType getRequestType();

    public Order getOrder();

    @Override
    public default int compareTo(Object o) {
        L3Request other = (L3Request)o;
        if(this.getOrder().getTimestamp()>other.getOrder().getTimestamp()){
            return (int)(this.getOrder().getTimestamp()-other.getOrder().getTimestamp());
        }
        else{
            return (int)(this.getOrder().getSeqNum()-other.getOrder().getSeqNum());
        }
    }

}
