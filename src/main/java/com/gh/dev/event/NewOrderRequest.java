package com.gh.dev.event;

import com.gh.dev.model.Order;

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
