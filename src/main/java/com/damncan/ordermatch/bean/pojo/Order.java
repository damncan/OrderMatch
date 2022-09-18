package com.damncan.ordermatch.bean.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Order {
    @JsonProperty("buyOrSell")
    private String buyOrSell;

    @JsonProperty("orderType")
    private String orderType;

    @JsonProperty("price")
    private int price;

    @JsonProperty("quantity")
    private int quantity;

    private LocalDateTime orderTime = LocalDateTime.now();
}
