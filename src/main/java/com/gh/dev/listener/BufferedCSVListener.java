package com.gh.dev.listener;

import com.gh.dev.calc.OrderBookEngine;
import com.gh.dev.event.L3Request;
import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

//Loads L3 file  with prespecified batch size -> converts to unified L3Request Model -> sends to OrderBookEngine for processing
public class BufferedCSVListener implements SourceListener{

    private final String file;
    private final int batchSize = 100;

    public BufferedCSVListener(String inputRequestFile) {
        this.file = inputRequestFile;
    }

    @Override
    public void process(OrderBookEngine orderBookEngine) throws InputReadException, OrderProcessingException, BBOException {
        try {
            try (Scanner scanner = new Scanner(Paths.get(file))) {
                boolean headerSkipped = false;
                List<String> linesInBatch = new ArrayList<>(batchSize);
                while (scanner.hasNextLine()) {
                    if (!headerSkipped) {
                        scanner.nextLine();
                        headerSkipped = true;
                    }
                    else if (linesInBatch.size() < batchSize) {
                        linesInBatch.add(scanner.nextLine());
                    }
                    else {
                        processBatch(linesInBatch, orderBookEngine);
                        linesInBatch.clear();
                    }
                }
                if(!linesInBatch.isEmpty()){
                    processBatch(linesInBatch, orderBookEngine);
                    linesInBatch.clear();
                }
            }
        } catch (IOException e) {
            throw new InputReadException(e.getMessage(),e.getCause());
        }

    }

    private void processBatch(List<String> lines, OrderBookEngine orderBookEngine) throws OrderProcessingException, BBOException {
        List<L3Request> requestsBatch = new LinkedList<>();
        for (String line : lines) {
            //TODO parse each line to create L3Request
        }
        orderBookEngine.processRequest(requestsBatch.stream());

    }

}
