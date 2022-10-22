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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    private ConcurrentLinkedDeque<Order> buyQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<Order> sellQueue = new ConcurrentLinkedDeque<>();

    private AtomicLong avgPrice = new AtomicLong(0);
    private AtomicInteger totalQuantity = new AtomicInteger(0);

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
                // if the order extracting from queue is "market price", then set the current market price for it
                if(buyOrder.getOrderType().equals("M")){
                    buyOrder.setPrice(avgPrice.intValue());
                }

                matchOrders.add(buyOrder);
                if (quantity > 0 && (orderType.equals("M") || buyOrder.getOrderType().equals("M") || buyOrder.getPrice() >= price)) {
                    int remainingQuantity = buyOrder.getQuantity();

                    // full match: the incoming order is totally matched
                    if (remainingQuantity > quantity) {
                        buyOrder.setQuantity(remainingQuantity - quantity);
                        quantity = 0;
                        break;
                    } else { // partial match: the incoming order is partially matched, so we need to move to the next unmatched order inside the queue
                        buyQueue.remove(buyOrder);
                        quantity -= remainingQuantity;
                    }
                }
            }

            // after matching, there still has some unmatched quantity inside the incoming order
            if (quantity > 0) {
                order.setQuantity(quantity);
                sellQueue.addLast(order);
            }
        } else if (buyOrSell.equals("B")) {
            for (Order sellOrder : sellQueue) {
                if(sellOrder.getOrderType().equals("M")){
                    sellOrder.setPrice(avgPrice.intValue());
                }

                matchOrders.add(sellOrder);
                if (quantity > 0 && (orderType.equals("M") || sellOrder.getOrderType().equals("M")  || sellOrder.getPrice() <= price)) {
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

        // update the average matched price
        if(OriQuantity - quantity > 0) {
            avgPrice.set((totalQuantity.get() * avgPrice.get() + price * (OriQuantity - quantity)) / (totalQuantity.get() + OriQuantity - quantity));
            totalQuantity.set(totalQuantity.get() + OriQuantity - quantity);
        }

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

    // private BigDecimal getAvgPrice(){
    //     RequestEntity<Void> request = RequestEntity
    //             .get(URI.create("http://localhost:8080/summary"))
    //             .accept(MediaType.APPLICATION_JSON)
    //             .build();
    //
    //     ResponseEntity<CombinedSummary> response = restTemplate
    //             .exchange(request,new ParameterizedTypeReference<>(){});
    //     CombinedSummary messages = response.getBody();
    //
    //     return messages.getTotalBuyAmount().add(messages.getTotalSellAmount()).subtract(messages.getTotalBuyQuantity().add(messages.getTotalSellQuantity()));
    // }
}
