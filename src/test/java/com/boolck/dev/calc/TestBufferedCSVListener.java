package com.boolck.dev.calc;

import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.listener.BufferedCSVListener;
import com.boolck.dev.listener.SourceListener;
import com.boolck.dev.model.BBO;
import com.boolck.dev.util.OrderBookRequestFileUtil;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.fail;

public class TestBufferedCSVListener {

    @Test
    public void testL3RequestProcessorSingleBatchInBulk() throws OrderProcessingException, InputReadException, IOException {
        Path filePath = Paths.get("src","test","resources");
        String inputFile = "l3_data_v3.csv";
        SourceListener listener = new BufferedCSVListener(
                filePath.resolve(inputFile).toFile().getAbsolutePath(),
                40000,
                26596);
        OrderBookEngine engine = getOrderBookEngine(filePath, inputFile);
        listener.process(engine);
        compareWithExpectedOutput(filePath, engine.getBBOList());
    }

    //change batchsize to 1 to consider each request as new stream
    @Test
    public void testL3RequestProcessAsStream() throws OrderProcessingException, InputReadException, IOException {
        Path filePath = Paths.get("src","test","resources");
        String inputFile = "l3_data_v3.csv";
        SourceListener listener = new BufferedCSVListener(
                filePath.resolve(inputFile).toFile().getAbsolutePath(),
                1,
                26596);
        OrderBookEngine engine = getOrderBookEngine(filePath, inputFile);
        listener.process(engine);
        compareWithExpectedOutput(filePath,engine.getBBOList());
    }

    /*
    tests unified L3 processing by splitting the incoming workload into batch and stream
    batch workload file is created as an example by taking top 10000 l3 requests
    remaining requests from 100001 till end makes the stream request file and is processed sequentially with batchsize=1
     */
    @Test
    public void testUnifiedL3ProcessRequest() throws OrderProcessingException, InputReadException, IOException {
        Path filePath = Paths.get("src","test","resources");
        String batchFile = "l3_data_v3_batch.csv";
        SourceListener batchListener = new BufferedCSVListener(
                filePath.resolve(batchFile).toFile().getAbsolutePath(),
                10000,
                Integer.MAX_VALUE);
        OrderBookEngine engine = getOrderBookEngine(filePath, batchFile);
        batchListener.process(engine);

        String streamFileSource = "l3_data_v3_stream.csv";
        SourceListener streamListener = new BufferedCSVListener(
                filePath.resolve(streamFileSource).toFile().getAbsolutePath(),
                1,
                16596);
        streamListener.process(engine);

        compareWithExpectedOutput(filePath,engine.getBBOList());
    }

    private void compareWithExpectedOutput(Path filePath, List<BBO> actualBBO) throws IOException {

        String l1ExpectedOutputFile = "expected_l1_data_v3.csv";
        BufferedReader l1Reader = new BufferedReader(new FileReader(filePath.resolve(l1ExpectedOutputFile).toFile()));
        l1Reader.readLine();
        CsvToBean<BBO> csvReader = new CsvToBeanBuilder<BBO>(l1Reader)
                .withType(BBO.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();
        List<BBO> expectedBBO = csvReader.parse();

        Map<BBO,BBO> mismatched = new HashMap<>();
        int min = Math.min(expectedBBO.size(),actualBBO.size());
        IntStream.range(0, min).forEach(i -> {
            if (!actualBBO.get(i).equals(expectedBBO.get(i))) {
                mismatched.put(actualBBO.get(i),expectedBBO.get(i));
            }
        });
        if(!mismatched.isEmpty()){
            fail("Found "+mismatched.size()+" mismatches in expected vs actual BBO "+mismatched);
        }
    }

    private OrderBookEngine getOrderBookEngine(Path filePath, String l3RequestFile) throws InputReadException {
        OrderBookRequestFileUtil.OrderBookAnalytics orderBookAnalytics =
                OrderBookRequestFileUtil.parseRequestFile(filePath.resolve(l3RequestFile).toFile().getAbsolutePath());
        int maxSeqWindow = orderBookAnalytics.getMaxSequenceNumberGap();
        String earliestSeqNum = orderBookAnalytics.getEarliestSeqNum();
        return new OrderBookEngine(earliestSeqNum,maxSeqWindow);
    }
}
