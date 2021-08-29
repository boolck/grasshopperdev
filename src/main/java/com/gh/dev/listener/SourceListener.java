package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;

//Source listener interface to pass l3 request via order book engine
@FunctionalInterface
public interface SourceListener {

    void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException;

}
