package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;


public interface SourceListener {

    void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, BBOException, InputReadException;
}
