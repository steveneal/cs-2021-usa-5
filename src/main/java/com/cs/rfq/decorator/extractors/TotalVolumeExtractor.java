package com.cs.rfq.decorator.extractors;

import com.cs.rfq.decorator.Rfq;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.joda.time.DateTime;
import static org.apache.spark.sql.functions.*;


import java.util.HashMap;
import java.util.Map;

/**
 * TotalVolumeExtractor calculates the total amount of an instrument traded with an entity over the past
 * week, month, and year.
 */
public class TotalVolumeExtractor extends AbstractExtractor implements RfqMetadataExtractor {

    /**
     * extractMetaData returns a map with qtyLastWeek, qtyLastMonth, qtyLastYear.
     * @param rfq as Rfq to supply the Isin and entity ID to match
     * @param session as SparkSession
     * @param trades as Dataset<Row> with previous trade data to extract from
     * @return Map<RfqMetadtaFieldNames, Object> with the extacted data
     */
    @Override
    public Map<RfqMetadataFieldNames, Object> extractMetaData(Rfq rfq, SparkSession session, Dataset<Row> trades) {
        long todayMs = getNow().getMillis();
        long pastWeekMs = getNow().minusWeeks(1).getMillis();
        long pastMonthMs = getNow().minusMonths(1).getMillis();
        long pastYearMs = getNow().minusYears(1).getMillis();

        Dataset<Row> filtered = trades
                .filter(trades.col("SecurityID").equalTo(rfq.getIsin()))
                .filter(trades.col("EntityId").equalTo(rfq.getEntityId()));


        Dataset<Row> tradesPastWeek = filtered.filter(trades.col("TradeDate").$greater$eq(new java.sql.Date(pastWeekMs)))
                .select(sum("LastQty"));
        Dataset<Row> tradesPastMonth = filtered.filter(trades.col("TradeDate").$greater$eq(new java.sql.Date(pastMonthMs)))
                .select(sum("LastQty"));
        Dataset<Row> tradesPastYear = filtered.filter(trades.col("TradeDate").$greater$eq(new java.sql.Date(pastYearMs)))
                .select(sum("LastQty"));

        Map<RfqMetadataFieldNames, Object> results = new HashMap<>();
        if(tradesPastWeek.first().get(0) != null) {
            results.put(RfqMetadataFieldNames.qtyLastWeek, tradesPastWeek.first().get(0));
        } else {
            results.put(RfqMetadataFieldNames.qtyLastWeek, 0);
        }
        if(tradesPastMonth.first().get(0) != null) {
            results.put(RfqMetadataFieldNames.qtyLastMonth, tradesPastMonth.first().get(0));
        } else {
            results.put(RfqMetadataFieldNames.qtyLastMonth, 0);
        }
        if(tradesPastYear.first().get(0) != null) {
            results.put(RfqMetadataFieldNames.qtyLastYear, tradesPastYear.first().get(0));
        } else {
            results.put(RfqMetadataFieldNames.qtyLastYear, 0);
        }

        return results;
    }
}
