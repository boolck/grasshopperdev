package com.gh.dev.calc;

import com.gh.dev.excp.BBOException;
import com.gh.dev.excp.InputReadException;
import com.gh.dev.excp.OrderProcessingException;
import com.gh.dev.listener.BufferedCSVListener;
import com.gh.dev.model.BBO;
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
    public void testL3RequestProcessor() throws OrderProcessingException, BBOException, InputReadException, IOException {
        Path filePath = Paths.get("src","test","resources");
        BufferedReader l1Reader = new BufferedReader(new FileReader(filePath.resolve("expected_l1_data_v3.csv").toFile()));
        l1Reader.readLine();
        CsvToBean<BBO> csvReader = new CsvToBeanBuilder<BBO>(l1Reader)
                .withType(BBO.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();
        List<BBO> expectedBBO = csvReader.parse();


        BufferedCSVListener listener = new BufferedCSVListener(filePath.resolve("l3_data_v3.csv").toFile().getAbsolutePath(),40000,16900);
        OrderBookEngine engine = new OrderBookEngine();
        listener.process(engine);
        List<BBO> actualBBO = engine.getBBOList();
        Map<BBO,BBO> mismatched = new HashMap<>();
        int min = Math.min(expectedBBO.size(),actualBBO.size());
        IntStream.range(0, min).forEach(i -> {
            BBO bbo = actualBBO.get(i);
            BBO o = expectedBBO.get(i);
            if (!bbo.equals(o)) {
                mismatched.put(bbo,o);
            }
        });
        if(!mismatched.isEmpty()){
            mismatched.forEach((key, value) -> System.out.printf("actual%s vs expected%s%n", key, value));
            fail("Found "+mismatched.size()+" mismatches in expected vs actual BBO, please check ");
        }
    }
}
