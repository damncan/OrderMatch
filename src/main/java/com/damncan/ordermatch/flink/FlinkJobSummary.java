package com.damncan.ordermatch.flink;

import com.damncan.ordermatch.bean.pojo.Summary;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Properties;

/**
 * This component starts a flink job, and its duties include:
 * <ul>
 * <li> Source: Polling trading result from kafka (topic name: cookedData) and assigning watermark for each record when each record has been pooled.
 * <li> Transformation: Dividing the original stream into two sub stream according to it's buy side or sell side. Then use a time window to aggregate the summary of trading, such as the total quantity and amount of buy/sell side orders, every 5 seconds.
 * <li> Sink: Storing the analysing result into a in-memory database, H2.
 * </ul>
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class FlinkJobSummary implements Serializable {
    @Value(value = "${kafka.bootstrapAddress}")
    private String kafkaBootstrapAddress;

    @Value(value = "${kafka.cookedDataTopic}")
    private String cookedDataTopic;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void execute() throws Exception {
        // Initialization
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Source
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", kafkaBootstrapAddress);
        properties.setProperty("group.id", "1");

        FlinkKafkaConsumer<ObjectNode> consumer = new FlinkKafkaConsumer<>(cookedDataTopic, new MatchResultSchema(), properties);
        consumer.setStartFromEarliest();

        consumer.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());
        // consumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5)));

        DataStream<ObjectNode> sourceStream = env.addSource(consumer);

        // Transformation
        DataStream<Summary> summaryStream = sourceStream
                .keyBy(k -> k.get("value").get("currentOrder").get("buyOrSell"))
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                // .trigger(ContinuousEventTimeTrigger.of(Time.seconds(5)))
                // .evictor(TimeEvictor.of(Time.seconds(0), true))
                .process((SummaryWindowOperator) applicationContext.getBean("summaryWindowOperator"))
                .setParallelism(2);

        // Sink
        summaryStream.addSink((SummarySink) applicationContext.getBean("summarySink"));

        // Execute Job
        new Thread(() -> {
            try {
                env.execute("Spring & Flink Integration");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
