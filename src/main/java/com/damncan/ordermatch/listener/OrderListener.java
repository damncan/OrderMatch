package com.damncan.ordermatch.listener;

import com.damncan.ordermatch.bean.pojo.Order;
import com.damncan.ordermatch.service.OrderMatchFIFOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * This component starts a kafka listener continuously polls raw trading data from kafka (topic name: rawData).
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Component
public class OrderListener {
    @Autowired
    private OrderMatchFIFOService orderMatchFIFOService;

    @KafkaListener(id = "listen1", topics = "rawData")
    public void listen1(Order order) {
        orderMatchFIFOService.matchOrder(order);
    }
}
