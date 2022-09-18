package com.damncan.ordermatch.bean.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Summary {
    private String buyOrSell;
    private BigDecimal totalOrder;
    private BigDecimal totalAmount;
    private BigDecimal totalQuantity;
}
