package com.gh.dev.event;

import com.gh.dev.model.Order;

//input TRADE l3 request with original order and  qty to be reduced
public class TradeOrderRequest implements L3Request {

    private final Order order;
    public long qtyToReduce;

    public TradeOrderRequest(Order existingOrder, long qtyToReduce){
        this.order = existingOrder;
        this.qtyToReduce = qtyToReduce;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.TRADE;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
