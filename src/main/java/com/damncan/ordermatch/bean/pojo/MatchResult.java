package com.damncan.ordermatch.bean.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MatchResult {
    private Order currentOrder;
    private List<Order> matchOrders;
    private int matchQuantity;
    private LocalDateTime matchTime = LocalDateTime.now();
}
