package com.gh.dev.event;

import com.gh.dev.model.Order;

public class CancelOrderRequest implements L3Request {
    private final Order order;
    public CancelOrderRequest(Order cancelledOrder){
        this.order = cancelledOrder;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.CANCEL;
    }

    @Override
    public Order getOrder() {
        return order;
    }


}
