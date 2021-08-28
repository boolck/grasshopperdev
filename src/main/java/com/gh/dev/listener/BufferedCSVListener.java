package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.event.L3Request;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.model.OrderBook;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
    1. Loads L3 file  with prespecified batch size
    2. converts to unified L3Request Model
    3. sends to OrderBookEngine for processing
 */
public class BufferedCSVListener implements SourceListener{
    //name of csv file having L3 incoming data
    private final String file;

    //default batch size
    private  int batchSize = 100;

    //limit for number of lines to be scanned. useful for integration test
    private  int limit = Integer.MAX_VALUE;

    //default constructor
    public BufferedCSVListener(String inputRequestFile) {
        this.file = inputRequestFile;
    }

    //overloader constructor with file, batch size and limit if any
    public BufferedCSVListener(String inputRequestFile,int batchSize,int limit) {
        this.file = inputRequestFile;
        this.batchSize = batchSize;
        this.limit = limit;
    }

    //parses the csv and calls orderbook engine to process the requests
    @Override
    public void process(OrderBookEngine orderBookEngine) throws InputReadException, OrderProcessingException {
        try {
            try (Scanner scanner = new Scanner(Paths.get(file))) {
                boolean headerSkipped = false;
                List<String> linesInBatch = new ArrayList<>(batchSize);
                int lineRead = 0;
                while (scanner.hasNextLine()) {
                    //skips header
                    if (!headerSkipped) {
                        scanner.nextLine();
                        headerSkipped = true;
                    }
                    //keep populating the batch until batchSize
                    else if (linesInBatch.size() < batchSize) {
                        linesInBatch.add(scanner.nextLine());
                    }
                    //else process this batch
                    else {
                        processBatch(linesInBatch, orderBookEngine);
                        linesInBatch.clear();
                    }
                    //if lines read exceeds limit, flush the batch
                    if(++lineRead>=limit){
                        processBatch(linesInBatch, orderBookEngine);
                        linesInBatch.clear();
                        return;
                    }
                }
                //any residual stream left to be processed
                if(!linesInBatch.isEmpty()){
                    processBatch(linesInBatch, orderBookEngine);
                    linesInBatch.clear();
                }
            }
        } catch (IOException e) {
            throw new InputReadException(e.getMessage(),e.getCause());
        }

    }

    /*
    this methodconverts each l3 csv line to correct L3 request (new/update/cancel/trade)
    then passes to order proessing engine for BBO processing
     last request is always passed as single request to simulate streaming.
     */

    private void processBatch(List<String> lines, OrderBookEngine orderBookEngine) throws OrderProcessingException {
        List<L3Request> requestsBatch =
                IntStream.range(0, lines.size() - 1)
                        .mapToObj(i -> OrderBook.parseOrderBookRow(lines.get(i))).
                        map(OrderBook::getL3Request)
                        .sorted(Comparator.comparing(r -> r.getOrder().getTimestamp()))
                        .collect(Collectors.toCollection(ArrayList::new));

        orderBookEngine.processRequest(requestsBatch.stream());

        L3Request lastRequest= OrderBook.parseOrderBookRow(lines.get(lines.size()-1)).getL3Request();
        orderBookEngine.processSingleAtomicRequest(lastRequest);
    }

}
