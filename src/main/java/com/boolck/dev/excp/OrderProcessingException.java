package com.boolck.dev.excp;

//exception to capture any order processing issues from order book engine
public class OrderProcessingException extends Exception {

    public OrderProcessingException(String msg){
        super(msg);
    }
}
