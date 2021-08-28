package com.gh.dev.event;


import com.gh.dev.model.Order;


public interface L3Request extends Comparable<L3Request> {

    enum RequestType{
        NEW,UPDATE,CANCEL,TRADE
    }

    RequestType getRequestType();

    Order getOrder();

    @Override
    default int compareTo(L3Request other) {
        return this.getOrder().compareSeqNum(other.getOrder());
    }

}
