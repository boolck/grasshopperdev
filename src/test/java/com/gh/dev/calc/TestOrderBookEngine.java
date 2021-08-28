package com.gh.dev.calc;

import com.gh.dev.event.*;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.BBO;
import com.gh.dev.model.Order;
import static com.gh.dev.model.Order.Side;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.gh.dev.event.TradeOrderRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestOrderBookEngine {

    OrderBookEngine engine = new OrderBookEngine("0",2);

    @Before
    public void beforeEach(){
        engine = new OrderBookEngine();
    }

    @Test
    public void testEmptyBBO() throws OrderProcessingException, BBOException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1)));
        BBO bbo = engine.getLatestBBO();
        assertNull(bbo);
    }

    @Test
    public void testNewOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sell1 = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        Order sell2 = orderBuilder.seqNum("4").id("s1").side(Side.SELL).price(12).qty(100).timestamp("4000").build();
        List<L3Request> newOrderRequests = Arrays.asList(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1), new NewOrderRequest(sell2));
        engine.processRequest(newOrderRequests.stream());
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("3",10,100,11,100,"3000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testCancelOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(150).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sell1 = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        List<L3Request> newOrderRequests = Arrays.asList(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1));
        engine.processRequest(newOrderRequests.stream());
        List<L3Request> cancelRequest = Collections.singletonList(new CancelOrderRequest(buy1));
        engine.processRequest(cancelRequest.stream());
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("1",9,100,11,100,"1000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testUpdateOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sell1 = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1)));
        Order updateOrder = new Order.OrderBuilder().id(buy1.getOrderId()).seqNum("4").side(Side.BUY).timestamp("4000").build();
        engine.processRequest(Stream.of(new UpdateOrderRequest(updateOrder,10.5,100)));
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("4",10.5,100,11,100,"4000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testTradeOrderWithQtyInConsideration() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sell1 = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1)));
        engine.processRequest(Stream.of(new TradeOrderRequest(buy1,20)));
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("1",10,80,11,100,"1000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testTradeOrderWithQtyExceeding() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sell1 = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        engine.processRequest(Stream.of(new NewOrderRequest(buy1), new NewOrderRequest(buy2), new NewOrderRequest(sell1)));
        engine.processRequest(Stream.of(new TradeOrderRequest(buy1,110)));
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("1",9,100,11,100,"1000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testCancelOutOfOrder() throws OrderProcessingException, BBOException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1_1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order sell1 = orderBuilder.seqNum("2").id("s1").side(Side.SELL).price(12).qty(100).timestamp("2000").build();
        Order buy1_4 = orderBuilder.seqNum("4").id("b1").side(Side.BUY).price(11).qty(100).timestamp("4000").build();
        List<L3Request> newOrderRequests = Arrays.asList(new NewOrderRequest(buy1_1), new NewOrderRequest(sell1), new NewOrderRequest(buy1_4));
        engine.processRequest(newOrderRequests.stream());
        BBO expectedBBO = new BBO("2",10,100,12,100,"2000");
        assertEquals(expectedBBO,engine.getLatestBBO());
        Order cancelbuy1 = new Order.OrderBuilder().id(buy1_1.getOrderId()).seqNum("3").side(Side.BUY).timestamp("3000").build();
        engine.processRequest(Stream.of(new CancelOrderRequest(cancelbuy1)));
        expectedBBO = new BBO("4",11,100,12,100,"4000");
        assertEquals(expectedBBO,engine.getLatestBBO());
    }

}
