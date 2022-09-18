package com.damncan.ordermatch.service;

import com.damncan.ordermatch.bean.pojo.Order;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;

@Deprecated
@Service
public class OrderMatchBestPriceService {

    ConcurrentSkipListMap<Integer, ConcurrentLinkedDeque<Order>> sellOrdersMap = new ConcurrentSkipListMap<>(
            Comparator.comparingInt(v -> v));

    ConcurrentSkipListMap<Integer, ConcurrentLinkedDeque<Order>> buyOrdersMap = new ConcurrentSkipListMap<>(
            Comparator.comparingInt(v -> v));

    public synchronized void orderMatch(Order order) {
        String orderType = order.getOrderType();
        String buyOrSell = order.getBuyOrSell();
        int quantity = order.getQuantity();
        int price = order.getPrice();

        if (buyOrSell.equals("S")) {
            while (quantity > 0 && !buyOrdersMap.isEmpty() && (orderType.equals("M") || buyOrdersMap.firstKey() >= price)) {
                ConcurrentLinkedDeque<Order> buyOrders = buyOrdersMap.get(buyOrdersMap.firstKey());
                Order matchedOrder = buyOrders.peekFirst();
                int remainingQuantity = matchedOrder.getQuantity();

                if (remainingQuantity > quantity) {
                    matchedOrder.setQuantity(remainingQuantity - quantity);
                    quantity = 0;
                } else {
                    buyOrders.pollFirst();
                    quantity -= remainingQuantity;
                }

                if (!buyOrdersMap.isEmpty() && buyOrdersMap.get(buyOrdersMap.firstKey()).isEmpty()) {
                    buyOrdersMap.pollFirstEntry();
                }
            }

            if (quantity > 0) {
                ConcurrentLinkedDeque orders = sellOrdersMap.getOrDefault(order.getPrice(), new ConcurrentLinkedDeque<>());
                order.setQuantity(quantity);
                orders.add(order);
                sellOrdersMap.put(order.getPrice(), orders);
            }
        } else if (buyOrSell.equals("B")) {
            while (quantity > 0 && !sellOrdersMap.isEmpty() && (orderType.equals("M") || sellOrdersMap.firstKey() <= price)) {
                ConcurrentLinkedDeque<Order> sellOrders = sellOrdersMap.get(sellOrdersMap.firstKey());
                Order matchedOrder = sellOrders.peekFirst();
                int remainingQuantity = matchedOrder.getQuantity();

                if (remainingQuantity > quantity) {
                    matchedOrder.setQuantity(remainingQuantity - quantity);
                    quantity = 0;
                } else {
                    sellOrders.pollFirst();
                    quantity -= remainingQuantity;
                }

                if (!sellOrdersMap.isEmpty() && sellOrdersMap.get(sellOrdersMap.firstKey()).isEmpty()) {
                    sellOrdersMap.pollFirstEntry();
                }
            }

            if (quantity > 0) {
                ConcurrentLinkedDeque orders = buyOrdersMap.getOrDefault(order.getPrice(), new ConcurrentLinkedDeque<>());
                order.setQuantity(quantity);
                orders.add(order);
                buyOrdersMap.put(order.getPrice(), orders);
            }
        }
    }

    public Map<Integer, ConcurrentLinkedDeque<Order>> getOrders(String orderType) {
        if (orderType.equals("S")) {
            return sellOrdersMap;
        } else if (orderType.equals("B")) {
            return buyOrdersMap;
        } else {
            sellOrdersMap = new ConcurrentSkipListMap<>(
                    Comparator.comparingInt(v -> v));
            buyOrdersMap = new ConcurrentSkipListMap<>(
                    Comparator.comparingInt(v -> v));

            return sellOrdersMap;
        }
    }
}
