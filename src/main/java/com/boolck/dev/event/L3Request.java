package com.boolck.dev.event;


import com.boolck.dev.model.Order;

// l3 request interfae to support all ops with  default ordering via seq number
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
