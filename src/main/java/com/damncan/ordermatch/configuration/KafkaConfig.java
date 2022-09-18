package com.damncan.ordermatch.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * This config is used to initialize two kafka topic, rawData and cookedData, to temporarily store trading raw data and matching result respectively.
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Configuration
public class KafkaConfig {
    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value(value = "${kakfa.rawDataTopic}")
    private String rawDataTopic;

    @Value(value = "${kafka.cookedDataTopic}")
    private String cookedDataTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicRawData() {
        return new NewTopic(rawDataTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic topicCookedData() {
        return new NewTopic(cookedDataTopic, 1, (short) 1);
    }
}
