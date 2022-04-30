package com.boolck.dev.calc;

import com.boolck.dev.event.*;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.model.BBO;
import com.boolck.dev.model.Order;

import java.util.*;
import java.util.stream.Stream;

import static java.util.AbstractMap.*;

//main calculator class for order request processing and BBO creation
public class OrderBookEngine {

    //bid(buy) & offer(ask) priority queues, sorted by price accordingly
    private final Queue<Order> bidsQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getSeqNumAsInt));
    private final Queue<Order> offerQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice).thenComparing(Order::getSeqNumAsInt));

    //orderIdMap to fetch existing orders in O(1) complexity
    private final Map<AbstractMap.SimpleEntry<String, Order.Side>,Order> orderIdMap = new HashMap<>();

    //puts out of order cancel/update orders without existing orders
    private final List<L3Request> outOfOrderRequests = new LinkedList<>();

    //maintains list of BBO
    private final List<BBO> bbo  = new ArrayList<>();

    //ordered set of out of sequence requests
    private final Set<L3Request> buffer = new TreeSet<>();

    //max gap in latest and past processed order till after which buffer is flushed
    private final int maxSeqWindow;

    //heads for bestBid and bestOffer from the queue
    private Order bestBid, bestOffer;

    //seqNum of latest past processed ordered, used to compare against new ordered in processing
    private String lastSeqNum ;

    /*
    initial sequence is defaulted to first-1 seqnum from the raw l3 data provided
    maxSeqWindow is max gap between any consecutive orders inferred from raw l3 data provided
    these can be passed for for any custom implementation to overloaded constructor
    */
    public OrderBookEngine(String initialSeqNum, int maxSeqWindow){
        this.lastSeqNum = initialSeqNum;
        this.maxSeqWindow = maxSeqWindow;
    }

    /*
    processes the micro batch of incoming requests.
    a buffer is used to hold them if not in sequence and processed in the end
    */
    public void processRequest(Stream<L3Request> requestStream) throws OrderProcessingException {
        Iterator<L3Request> iterator = requestStream.iterator();
        while (iterator.hasNext()) {
            L3Request request = iterator.next();
            checkSequenceAndProcessBuffer(request);
        }
        //process out of order messages if cancel/update is without existing order\
        this.processOutOfOrderMessages();
    }

    public void checkSequenceAndProcessBuffer(L3Request request) throws OrderProcessingException {
        checkSequenceAndProcessRequest(request);
        //process out of sync orders based on sequence number. if buffers exceed maximum allowed, process directly

        processOutOfOrderMessages();
    }

    //check if sequence number is not in sync else process
    private void checkSequenceAndProcessRequest(L3Request request) throws OrderProcessingException {
        if(isRequestOutOrOrder(request)){
            buffer.add(request);
            return ;
        }
        processThisAtomicRequest(request);
    }

    //compares the BigInteger equivalent for seqnum string and if there is gap of more than 1, considers out of order.
    private boolean isRequestOutOrOrder(L3Request request) {
        return request.getOrder().compareSeqNum(lastSeqNum) > 1;
    }

    //processes all buffered messages one by one. skips checking sequence if directpublish is enabled
    private void processBufferedMessages(boolean directPublish) throws  OrderProcessingException {
        if(directPublish){
            for(L3Request request1 : buffer){
                this.processThisAtomicRequest(request1);
            }
            buffer.clear();
        }
        else {
            Set<L3Request> clonedBuffer = new TreeSet<>(buffer);
            for (L3Request next : clonedBuffer) {
                buffer.remove(next);
                this.checkSequenceAndProcessRequest(next);
            }
        }
    }

    //processes both buffered messages and out of sync cancel/update messages
    public void processOutOfOrderMessages() throws OrderProcessingException {
        processBufferedMessages(buffer.size()> maxSeqWindow);
        if(!outOfOrderRequests.isEmpty()){
            List<L3Request> clonedList  = new LinkedList<>(outOfOrderRequests);
            outOfOrderRequests.clear();
            for(L3Request request : clonedList){
                this.checkSequenceAndProcessRequest(request);
            }
        }

    }

    //processes single order  request & updates lastseqnum
    private void processThisAtomicRequest(L3Request request) throws OrderProcessingException {
        L3Request.RequestType requestType = request.getRequestType();

        switch (requestType) {
            case NEW: {
                processNewOrder((NewOrderRequest) request);
                break;
            }
            case UPDATE: {
                processUpdateOrder((UpdateOrderRequest) request);
                break;
            }
            case CANCEL: {
                processCancelOrder((CancelOrderRequest)request);
                break;
            }
            case TRADE: {
                processTradeOrder((TradeOrderRequest)request);
                break;
            }
            default:
                throw new OrderProcessingException("Invalid requestType received " + requestType);
        }
        lastSeqNum = request.getOrder().getSeqNum();
    }

    //processes new order request
    private void processNewOrder(NewOrderRequest newOrderRequest) {
        Order newOrder = newOrderRequest.getOrder();
        Queue<Order> orderQ = newOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(newOrder.getOrderId(), newOrder.getSide());
        if(orderIdMap.containsKey(key)) {
            //new order already exists, add to out of order q
            outOfOrderRequests.add(newOrderRequest);
        }
        else{
            orderQ.add(newOrder);
            orderIdMap.put(key,newOrder);
            updateBBO(newOrder);
        }
    }

    private void processUpdateOrder(UpdateOrderRequest updateOrderRequest)  {
        Order updateOrder = updateOrderRequest.getOrder();
        Queue<Order> orderQ = updateOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(updateOrder.getOrderId(), updateOrder.getSide());
        Order existingOrder = orderIdMap.get(key);
        if(existingOrder!=null) {
            //existing order should exist before update
            boolean isRemoved = orderQ.remove(updateOrder);
            if (isRemoved) {
                updateOrder.setQty(updateOrderRequest.newQty);
                updateOrder.setPrice(updateOrderRequest.newPrice);
                orderQ.add(updateOrder);
                updateBBO(updateOrder);
            } else {
                outOfOrderRequests.add(updateOrderRequest);
            }
        }
        else{
            outOfOrderRequests.add(updateOrderRequest);
        }

    }


    private void processCancelOrder(CancelOrderRequest cancelOrderRequest)  {
        Order cancelOrder = cancelOrderRequest.getOrder();
        Queue<Order> orderQ = cancelOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(cancelOrder.getOrderId(), cancelOrder.getSide());
        Order existingOrder = orderIdMap.get(key);
        if(existingOrder!=null) {
            //existing order should exist before delete
            boolean isRemoved = orderQ.remove(cancelOrder);
            if (isRemoved) {
                cancelOrder.setPrice(existingOrder.getPrice());
                cancelOrder.setQty(existingOrder.getQty());
                orderIdMap.remove(key);
                updateBBO(cancelOrder);
            } else {
                outOfOrderRequests.add(cancelOrderRequest);
            }
        }
        else{
            outOfOrderRequests.add(cancelOrderRequest);
        }
    }

    private void processTradeOrder(TradeOrderRequest request) {
        Order tradeOrder = request.getOrder();
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(tradeOrder.getOrderId(), tradeOrder.getSide());
        Order existingOrder = orderIdMap.get(key);
        //existing order should exist before tradematch
        if(existingOrder!=null) {
            //if reduction qty > existing qty, cancel the trade
            long newQty = existingOrder.getQty() - request.qtyToReduce;
            if(newQty <= 0 ){
                processCancelOrder(new CancelOrderRequest(tradeOrder));
            }
            //else reduce the qty by updating order
            else{
                processUpdateOrder(new UpdateOrderRequest(tradeOrder,existingOrder.getPrice(),newQty));
            }
        }
        else{
            outOfOrderRequests.add(request);
        }
    }

    //updates BBO is eligible update to ask or offer is available
    private void updateBBO(Order orderInProcess)  {
        if (bidsQueue.isEmpty() || offerQueue.isEmpty()) {
            return;
        }

        Order bidHead = bidsQueue.peek();
        Order offerHead = offerQueue.peek();
        if (shouldUpdateBBO(bidHead, offerHead) || doesQtyChangedAtHead(orderInProcess) ) {
            //gets nearest orders ordered by sequence number
            this.bestBid = getValidOrder(bidsQueue,orderInProcess) ;
            this.bestOffer = getValidOrder(offerQueue,orderInProcess) ;
            if(bestBid != null  && bestOffer != null){
                long cumulativeBidQty = this.getCumulativeQty(bestBid,bidsQueue);
                long cumulativeOfferQty = this.getCumulativeQty(bestOffer,offerQueue);
                BBO latestBBO = new BBO(
                        orderInProcess.getSeqNum(),
                        bestBid.getPrice(),
                        cumulativeBidQty,
                        bestOffer.getPrice(),
                        cumulativeOfferQty,
                        orderInProcess.getTimestamp());

                bbo.add(latestBBO);
            }
        }
    }

    //compares if new order has changed qty for existing BBO to trigger a new BBO update
    private boolean doesQtyChangedAtHead(Order orderInProcess) {
        BBO latestBBO = getLatestBBO();
        if(orderInProcess.isBuy() && Double.compare(orderInProcess.getPrice(),latestBBO.getBidPrice())==0 && orderInProcess.getQty()!=latestBBO.getBidQty()){
            return true;
        }
        return !orderInProcess.isBuy() && Double.compare(orderInProcess.getPrice(),latestBBO.getAskPrice())==0 && orderInProcess.getQty()!=latestBBO.getAskQty();
    }

    //gets the latest and nearest order from queue based on orderin process. might be redundant as buffer is implemented
    private Order getValidOrder(Queue<Order> ordersQueue,Order orderInProcess){
        LinkedList<Order> polledOrders = new LinkedList<>();
        while(!ordersQueue.isEmpty()
                && ordersQueue.peek()!=null
                && ordersQueue.peek().compareSeqNum(orderInProcess)>0){
            polledOrders.add(ordersQueue.poll());
        }
        Order validInSequenceOrder  = !ordersQueue.isEmpty() ? ordersQueue.peek() : polledOrders.getFirst();
        ordersQueue.addAll(polledOrders);
        return validInSequenceOrder;
    }

    //checks if head of queue is not same as best bid/ask thus triggering a new BBO update
    private boolean shouldUpdateBBO(Order bidHead, Order offerHead) {
        return !bidHead.equals(bestBid)
                || bidHead.getQty() != bestBid.getQty()
                || Double.compare(bidHead.getPrice(), bestBid.getPrice()) != 0
                || !offerHead.equals(bestOffer)
                || offerHead.getQty() != bestOffer.getQty()
                || Double.compare(offerHead.getPrice(), bestOffer.getPrice()) != 0;
    }

    //gets the cumulative quantity from all nodes with same best price
    private long getCumulativeQty(Order bestOrder, Queue<Order> orderQueue) {
        List<Order> polledOrders = new LinkedList<>();
        while(!orderQueue.isEmpty() && orderQueue.peek()!=null && orderQueue.peek().getPrice()==bestOrder.getPrice()){
            polledOrders.add(orderQueue.poll());
        }
        orderQueue.addAll(polledOrders);
        return polledOrders.isEmpty() && !orderQueue.isEmpty() ? orderQueue.peek().getQty() : polledOrders.stream().mapToLong(Order::getQty).sum();
    }

    public List<BBO> getBBOList() {
        return bbo;
    }

    public BBO getLatestBBO() {
        return bbo.isEmpty() ? null : bbo.get(bbo.size()-1);
    }
}
