package com.boolck.dev.listener;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.calc.OrderBookEngine;

//Source listener interface to pass l3 request via order book engine
@FunctionalInterface
public interface SourceListener {

    void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException;

}
