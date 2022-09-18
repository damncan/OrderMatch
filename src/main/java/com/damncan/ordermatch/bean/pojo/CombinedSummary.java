package com.damncan.ordermatch.bean.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CombinedSummary {
    private BigDecimal totalBuyOrder;
    private BigDecimal totalSellOrder;
    private BigDecimal totalBuyAmount;
    private BigDecimal totalSellAmount;
    private BigDecimal totalBuyQuantity;
    private BigDecimal totalSellQuantity;
    private LocalDateTime analysedTime;
}
