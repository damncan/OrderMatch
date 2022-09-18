package com.damncan.ordermatch.flink;

import com.damncan.ordermatch.bean.pojo.Summary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * This component is used to analyze the summary of trading within each 5-second time window.
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class SummaryWindowOperator extends ProcessWindowFunction<ObjectNode, Summary, JsonNode, TimeWindow> {
    public SummaryWindowOperator() {
    }

    @Override
    public void process(JsonNode key, Context context, Iterable<ObjectNode> input, Collector<Summary> output) throws Exception {
        BigDecimal totalOrder = BigDecimal.valueOf(0);
        BigDecimal totalAmount = BigDecimal.valueOf(0);
        BigDecimal totalQuantity = BigDecimal.valueOf(0);

        for (ObjectNode in : input) {
            totalOrder = totalOrder.add(BigDecimal.valueOf(1));

            BigDecimal matchQuantity = BigDecimal.valueOf(in.get("value").get("matchQuantity").longValue());
            totalQuantity = totalQuantity.add(matchQuantity);

            BigDecimal matchPrice = BigDecimal.valueOf(in.get("value").get("currentOrder").get("price").longValue());
            totalAmount = totalAmount.add(matchQuantity.multiply(matchPrice));

            System.out.println(in);
        }

        output.collect(new Summary(key.asText(), totalOrder, totalAmount, totalQuantity));
    }
}
