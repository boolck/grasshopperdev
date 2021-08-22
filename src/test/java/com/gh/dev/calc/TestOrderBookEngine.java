package com.gh.dev.calc;

import com.gh.dev.event.*;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.Order;
import static com.gh.dev.model.Order.Side;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestOrderBookEngine {
    OrderBookEngine engine = new OrderBookEngine();

    @Before
    public void beforeEach(){
        engine = new OrderBookEngine();
    }

    @Test
    public void testEmptyBBO() throws OrderProcessingException, BBOException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum(1).id("b1").side(Side.BUY).price(10).qty(100).timestamp(1000).build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1)));
        BBO bbo = engine.getBbo();
        assertNull(bbo);
    }

    @Test
    public void testNewOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum(1).id("b1").side(Side.BUY).price(10).qty(100).timestamp(1000).build();
        Order buy2 = orderBuilder.seqNum(2).id("b2").side(Side.BUY).price(9).qty(100).timestamp(2000).build();
        Order sell1 = orderBuilder.seqNum(3).id("s1").side(Side.SELL).price(11).qty(100).timestamp(3000).build();
        List<L3Request> newOrderRequests = Arrays.asList(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1));
        engine.processRequest(newOrderRequests.stream());
        BBO bbo = engine.getBbo();
        BBO expectedBBO = new BBO(3,9,100,11,100,3000);
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testCancelOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum(1).id("b1").side(Side.BUY).price(10).qty(150).timestamp(1000).build();
        Order buy2 = orderBuilder.seqNum(2).id("b2").side(Side.BUY).price(9).qty(100).timestamp(2000).build();
        Order sell1 = orderBuilder.seqNum(3).id("s1").side(Side.SELL).price(11).qty(100).timestamp(3000).build();
        List<L3Request> newOrderRequests = Arrays.asList(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1));
        engine.processRequest(newOrderRequests.stream());
        List<L3Request> cancelRequest = Collections.singletonList(new CancelOrderRequest(buy2));
        engine.processRequest(cancelRequest.stream());
        BBO bbo = engine.getBbo();
        BBO expectedBBO = new BBO(3,10,50,11,100,3000);
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testUpdateOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum(1).id("b1").side(Side.BUY).price(10).qty(100).timestamp(1000).build();
        Order buy2 = orderBuilder.seqNum(2).id("b2").side(Side.BUY).price(9).qty(100).timestamp(2000).build();
        Order sell1 = orderBuilder.seqNum(3).id("s1").side(Side.SELL).price(11).qty(100).timestamp(3000).build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1)));
        engine.processRequest(Stream.of(new UpdateOrderRequest(sell1,12,100)));
        BBO bbo = engine.getBbo();
        BBO expectedBBO = new BBO(3,9,100,12,100,3000);
        assertEquals(expectedBBO,bbo);
    }

    @Test
    (expected = OrderProcessingException.class)
    public void testInvalidOperation() throws OrderProcessingException, BBOException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy2 = orderBuilder.seqNum(2).id("b2").side(Side.BUY).price(9).qty(100).timestamp(2000).build();
        List<L3Request> cancelRequest = Collections.singletonList(new CancelOrderRequest(buy2));
        engine.processRequest(cancelRequest.stream());
        BBO bbo = engine.getBbo();
    }


}
