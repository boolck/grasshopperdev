package com.boolck.dev;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.listener.BufferedCSVListener;
import com.boolck.dev.listener.SourceListener;
import com.boolck.dev.model.BBO;
import com.boolck.dev.util.OrderBookRequestFileUtil;
import com.boolck.dev.calc.OrderBookEngine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/*
main entry class to run the program, it takes input from l3 request file csv
in order to process each request in streaming, it uses BufferedCSVListener with batch size as unity
an example of unified (batch + streaming) is tested in TestBufferedCSVListener.testUnifiedL3ProcessRequest
 */
public class ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger("ApplicationRunner.class");

    public static void main(String... args) throws InputReadException, OrderProcessingException {
        if (args == null || args.length < 1) {
            LOGGER.warning("No Input L3 request files provided");
            return;
        }

        String inputL3File = Objects.requireNonNull(args)[0];
        Path inputFilePath = Paths.get(inputL3File);
        if (!inputFilePath.toFile().canRead() ) {
            LOGGER.severe("file paths for " + inputL3File + " is not correct, exiting ");
            return;
        }

        OrderBookRequestFileUtil.OrderBookAnalytics orderBookAnalytics =
                OrderBookRequestFileUtil.parseRequestFile(inputFilePath.toFile().getAbsolutePath());
        int maxSeqWindow = orderBookAnalytics.getMaxSequenceNumberGap();
        String earliestSeqNum = orderBookAnalytics.getEarliestSeqNum();
        OrderBookEngine engine = new OrderBookEngine(earliestSeqNum, maxSeqWindow);

        SourceListener streamingListener = new BufferedCSVListener(
                inputFilePath.toFile().getAbsolutePath());

        streamingListener.process(engine);

        List<BBO> bboList = engine.getBBOList();
        bboList.forEach(x -> LOGGER.info(x.toString()));

    }
}
