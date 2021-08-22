package com.gh.dev.calc;

import com.gh.dev.event.*;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.Order;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

public class OrderBookEngine {

    private final Comparator<Order> orderComparator = Comparator.comparing(Order::getPrice);

    private final Queue<Order> bidsQueue = new PriorityQueue<>(orderComparator.reversed());
    private final Queue<Order> offerQueue = new PriorityQueue<>(orderComparator);
    private Order bestBid, bestOffer;
    private BBO bbo;


    public void processRequest(Stream<L3Request> requestStream) throws OrderProcessingException, BBOException {
        Stream<L3Request> sorted_time_seqnnum = requestStream.sorted();
        Iterator<L3Request> iterator = sorted_time_seqnnum.iterator();
        while (iterator.hasNext()) {
            L3Request request = iterator.next();
            L3Request.RequestType requestType = request.getRequestType();

            switch (requestType) {
                case NEW: {
                    NewOrderRequest newOrderRequest = (NewOrderRequest) request;
                    processNewOrder(newOrderRequest.getOrder());
                    break;
                }

                case UPDATE: {
                    UpdateOrderRequest newOrderRequest = (UpdateOrderRequest) request;
                    processUpdateOrder(newOrderRequest.getOrder(), newOrderRequest.newPrice, newOrderRequest.newQty);
                    break;
                }
                case CANCEL: {
                    CancelOrderRequest newOrderRequest = (CancelOrderRequest) request;
                    processCancelOrder(newOrderRequest.getOrder());
                    break;
                }
                case TRADE: {
                    break;
                }
                default:
                    throw new OrderProcessingException("Invalid requestType received " + requestType);
            }
        }
    }

    private void processUpdateOrder(Order updateOrder, double newPrice, long newQty) throws OrderProcessingException, BBOException {
        Queue<Order> orderQ = updateOrder.isBuy() ? bidsQueue : offerQueue;
        if (!orderQ.remove(updateOrder)) {
            throw new OrderProcessingException("Order not found " + updateOrder.toString());
        }
        updateOrder.setQty(newQty);
        updateOrder.setPrice(newPrice);
        orderQ.add(updateOrder);
        updateBBO();
    }

    private void processCancelOrder(Order cancelOrder) throws OrderProcessingException, BBOException {
        Queue<Order> orderQ = cancelOrder.isBuy() ? bidsQueue : offerQueue;

        boolean isRemoved = orderQ.remove(cancelOrder);

        if (isRemoved) {
            updateBBO();
        } else {
            throw new OrderProcessingException("Order not found " + cancelOrder.toString());
        }
    }

    private void processNewOrder(Order newOrder) throws BBOException {
        Queue<Order> orderQ = newOrder.isBuy() ? bidsQueue : offerQueue;
        Queue<Order> oppositeQ = newOrder.isBuy() ? offerQueue : bidsQueue;
        orderQ.add(newOrder);
        if (!oppositeQ.isEmpty()) {
            Order oppositeOrder = oppositeQ.peek();
            if (newOrder.getQty() < oppositeOrder.getQty()) {
                oppositeOrder.setQty(oppositeOrder.getQty() - newOrder.getQty());
            } else if (newOrder.getQty() > oppositeOrder.getQty()) {
                oppositeQ.poll();
                newOrder.setQty(newOrder.getQty() - oppositeOrder.getQty());
                processNewOrder(newOrder);
            }
            //matched
            else {
                oppositeQ.poll();
            }
            updateBBO();
        }
    }

    private void updateBBO() throws BBOException {
        if (bidsQueue.isEmpty()) {
            throw new BBOException("BidsQueue is empty");
        }
        if (offerQueue.isEmpty()) {
            throw new BBOException("OfferQueue is empty");
        }
        Order bidHead = bidsQueue.peek();
        Order offerHead = offerQueue.peek();
        if (!bidHead.equals(bestBid) || !offerHead.equals(bestOffer)) {
            this.bestBid = bidHead;
            this.bestOffer = offerHead;
        }
        if (bestOffer.getSeqNum() > bestBid.getSeqNum()) {
            bbo = new BBO(bestOffer.getSeqNum(), bestBid.getPrice(), bestBid.getQty(), bestOffer.getPrice(), bestOffer.getQty(), bestOffer.getTimestamp());
        } else {
            bbo = new BBO(bestBid.getSeqNum(), bestBid.getPrice(), bestBid.getQty(), bestOffer.getPrice(), bestOffer.getQty(), bestBid.getTimestamp());
        }
    }

    public BBO getBbo() {
        return bbo;
    }

}
