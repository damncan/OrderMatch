package com.damncan.ordermatch.service;

import com.damncan.ordermatch.bean.entity.SummaryEntity;
import com.damncan.ordermatch.bean.pojo.CombinedSummary;
import com.damncan.ordermatch.bean.pojo.MatchResult;
import com.damncan.ordermatch.bean.pojo.Order;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This service provide 3 methods.
 * <ul>
 * <li> matchOrder: When a buy/sell order has been polled from kafka (topic name: rawData), this method is used to match it with those unmatched orders in the queue. After matching order, it will send the matched result into kafka (topic name: cookedData).
 * <li> getOrders: Return all unmatched orders from each queue respectively according to the request parameter. (B for buy orders, S for sell orders, R for reset two queues)
 * <li> getSummary: Get matched summary from in-memory DB (H2).
 * </ul>
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Service
public class OrderMatchFIFOService {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value(value = "${kakfa.rawDataTopic}")
    private String rawDataTopic;

    @Value(value = "${kafka.cookedDataTopic}")
    private String cookedDataTopic;

    ConcurrentLinkedDeque<Order> buyQueue = new ConcurrentLinkedDeque<>();
    ConcurrentLinkedDeque<Order> sellQueue = new ConcurrentLinkedDeque<>();

    public void addToKafkaQueue(Order order) {
        kafkaTemplate.send(rawDataTopic, order);
    }

    public void matchOrder(Order order) {
        MatchResult matchResult = new MatchResult();
        matchResult.setCurrentOrder(order);
        List<Order> matchOrders = new ArrayList<>();

        String orderType = order.getOrderType();
        String buyOrSell = order.getBuyOrSell();
        int OriQuantity = order.getQuantity();
        int quantity = OriQuantity;
        int price = order.getPrice();

        if (buyOrSell.equals("S")) {
            for (Order buyOrder : buyQueue) {
                matchOrders.add(buyOrder);
                if (quantity > 0 && (orderType.equals("M") || buyOrder.getPrice() >= price)) {
                    int remainingQuantity = buyOrder.getQuantity();

                    if (remainingQuantity > quantity) {
                        buyOrder.setQuantity(remainingQuantity - quantity);
                        quantity = 0;
                        break;
                    } else {
                        buyQueue.remove(buyOrder);
                        quantity -= remainingQuantity;
                    }
                }
            }

            if (quantity > 0) {
                order.setQuantity(quantity);
                sellQueue.addLast(order);
            }
        } else if (buyOrSell.equals("B")) {
            for (Order sellOrder : sellQueue) {
                matchOrders.add(sellOrder);
                if (quantity > 0 && (orderType.equals("M") || sellOrder.getPrice() <= price)) {
                    int remainingQuantity = sellOrder.getQuantity();

                    if (remainingQuantity > quantity) {
                        sellOrder.setQuantity(remainingQuantity - quantity);
                        quantity = 0;
                        break;
                    } else {
                        sellQueue.remove(sellOrder);
                        quantity -= remainingQuantity;
                    }
                }
            }

            if (quantity > 0) {
                order.setQuantity(quantity);
                buyQueue.addLast(order);
            }
        }

        matchResult.setMatchOrders(matchOrders);
        matchResult.setMatchQuantity(OriQuantity - quantity);

        kafkaTemplate.send(cookedDataTopic, matchResult);
    }

    public ConcurrentLinkedDeque<Order> getOrders(String buyOrSell) {
        switch (buyOrSell) {
            case "S":
                return sellQueue;
            case "B":
                return buyQueue;
            case "R":
                buyQueue = new ConcurrentLinkedDeque<>();
                sellQueue = new ConcurrentLinkedDeque<>();
                return sellQueue;
        }
        return null;
    }

    public CombinedSummary getSummary() {
        EntityManager entityManager = Persistence.createEntityManagerFactory("FlinkSink").createEntityManager();

        CombinedSummary result = new CombinedSummary();
        BeanUtils.copyProperties(entityManager.find(SummaryEntity.class, 1) == null ? new CombinedSummary() : entityManager.find(SummaryEntity.class, 1), result);

        entityManager.close();
        return result;
    }
}
