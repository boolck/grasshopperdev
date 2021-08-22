package com.gh.dev.event;

import com.gh.dev.model.Order;

public class UpdateOrderRequest implements L3Request {
    private final Order order;
    public double newPrice;
    public long newQty;

    public UpdateOrderRequest(Order existingOrder, double newPrice, long newQty){
        this.order = existingOrder;
        this.newPrice = newPrice;
        this.newQty = newQty;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.UPDATE;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
