package com.damncan.ordermatch.controller;

import com.damncan.ordermatch.bean.pojo.CombinedSummary;
import com.damncan.ordermatch.bean.pojo.Order;
import com.damncan.ordermatch.service.OrderMatchFIFOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This controller provides 3 APIs.
 * <ul>
 * <li> POST /order: add order (buy order/sell order) into kafka.
 * <li> GET /orders/{buyOrSell}: get unmatched buy/sell orders from queue.
 * <li> GET /summary: get matched summary from in-memory DB (H2).
 * </ul>
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@RestController
public class OrderMatchController {
    @Autowired
    private OrderMatchFIFOService orderMatchFIFOService;

    @PostMapping(path = "order",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> addOrder(@RequestBody Order order) {
        orderMatchFIFOService.addToKafkaQueue(order);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/orders/{buyOrSell}")
    public ResponseEntity<ConcurrentLinkedDeque<Order>> getOrders(@PathVariable("buyOrSell") String buyOrSell) {
        return new ResponseEntity<>(orderMatchFIFOService.getOrders(buyOrSell), HttpStatus.CREATED);
    }

    @GetMapping("/summary")
    public ResponseEntity<CombinedSummary> getSummary() {
        return new ResponseEntity<>(orderMatchFIFOService.getSummary(), HttpStatus.CREATED);
    }
}
