package com.cs.rfq.decorator.extractors;

import com.cs.rfq.decorator.Rfq;
import com.cs.rfq.decorator.TradeDataLoader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AverageTradePriceTest extends AbstractSparkUnitTest {

    private Rfq rfq;
    Dataset<Row> trades;
    Dataset<Row> noMatches;

    @BeforeEach
    public void setup() {
        rfq = new Rfq();
        rfq.setEntityId(5561279226039690843l);
        rfq.setIsin("AT0000A0VRQ6");
        rfq.setPrice(130.0);

        String filePath = getClass().getResource("average-traded-1.json").getPath();
        String noMatchPath = getClass().getResource("volume-traded-1.json").getPath();
        trades = new TradeDataLoader().loadTrades(session, filePath);
        noMatches = new TradeDataLoader().loadTrades(session, noMatchPath);
    }

    @Test
    public void checkAverageOfTestTrades() {
        AverageTradePriceExtractor extractor = new AverageTradePriceExtractor();
        extractor.setSince("2021-07-30");

        Map<RfqMetadataFieldNames, Object> meta = extractor.extractMetaData(rfq, session, trades);

        Object result = meta.get(RfqMetadataFieldNames.averageTradedPrice);

        assertEquals(138, (int) result);
    }

    @Test
    public void checkNoMatchesonTrades() {
        AverageTradePriceExtractor extractor = new AverageTradePriceExtractor();
        extractor.setSince("2021-07-30");

        Map<RfqMetadataFieldNames, Object> meta = extractor.extractMetaData(rfq, session, noMatches);

        Object result = meta.get(RfqMetadataFieldNames.averageTradedPrice);

        assertEquals(0, (int) result);
    }
}
