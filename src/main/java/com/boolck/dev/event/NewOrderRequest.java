package com.boolck.dev.event;

import com.boolck.dev.model.Order;

//input NEW l3 request with  order to be inserted
public class NewOrderRequest implements L3Request {

    private final Order order;

    public NewOrderRequest(Order order){
        this.order = order;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.NEW;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
