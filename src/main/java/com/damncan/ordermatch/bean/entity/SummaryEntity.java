package com.damncan.ordermatch.bean.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_SUMMARY")
@Getter
@Setter
@NoArgsConstructor
public class SummaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "TOTAL_BUY_ORDER")
    BigDecimal totalBuyOrder;

    @Column(name = "TOTAL_SELL_ORDER")
    BigDecimal totalSellOrder;

    @Column(name = "TOTAL_BUY_AMOUNT")
    BigDecimal totalBuyAmount;

    @Column(name = "TOTAL_SELL_AMOUNT")
    BigDecimal totalSellAmount;

    @Column(name = "TOTAL_BUY_QUANTITY")
    BigDecimal totalBuyQuantity;

    @Column(name = "TOTAL_SELL_QUANTITY")
    BigDecimal totalSellQuantity;

    @Column(name = "ANALYSED_TIME")
    LocalDateTime analysedTime = LocalDateTime.now();
}
