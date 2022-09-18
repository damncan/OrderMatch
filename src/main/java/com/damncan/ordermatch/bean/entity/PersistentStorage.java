package com.damncan.ordermatch.bean.entity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class PersistentStorage implements Serializable {
    private AtomicReference<BigDecimal> totalBuyOrder = new AtomicReference<>(new BigDecimal("0"));
    private AtomicReference<BigDecimal> totalSellOrder = new AtomicReference<>(new BigDecimal("0"));
    private AtomicReference<BigDecimal> totalBuyAmount = new AtomicReference<>(new BigDecimal("0.00"));
    private AtomicReference<BigDecimal> totalSellAmount = new AtomicReference<>(new BigDecimal("0.00"));

    private AtomicReference<BigDecimal> totalBuyQuantity = new AtomicReference<>(new BigDecimal("0"));

    private AtomicReference<BigDecimal> totalSellQuantity = new AtomicReference<>(new BigDecimal("0"));

    public PersistentStorage() {
    }

    public AtomicReference<BigDecimal> getTotalBuyOrder() {
        return totalBuyOrder;
    }

    public void setTotalBuyOrder(AtomicReference<BigDecimal> totalBuyOrder) {
        this.totalBuyOrder = totalBuyOrder;
    }

    public void addTotalBuyOrder(BigDecimal buyOrder) {
        this.totalBuyOrder.updateAndGet(v -> v.add(buyOrder));
    }

    public AtomicReference<BigDecimal> getTotalSellOrder() {
        return totalSellOrder;
    }

    public void setTotalSellOrder(AtomicReference<BigDecimal> totalSellOrder) {
        this.totalSellOrder = totalSellOrder;
    }

    public void addTotalSellOrder(BigDecimal sellOrder) {
        this.totalSellOrder.updateAndGet(v -> v.add(sellOrder));
    }

    public AtomicReference<BigDecimal> getTotalBuyAmount() {
        return totalBuyAmount;
    }

    public void setTotalBuyAmount(AtomicReference<BigDecimal> totalBuyAmount) {
        this.totalBuyAmount = totalBuyAmount;
    }

    public void addTotalBuyAmount(BigDecimal buyAmount) {
        this.totalBuyAmount.updateAndGet(v -> v.add(buyAmount));
    }

    public AtomicReference<BigDecimal> getTotalSellAmount() {
        return totalSellAmount;
    }

    public void setTotalSellAmount(AtomicReference<BigDecimal> totalSellAmount) {
        this.totalSellAmount = totalSellAmount;
    }

    public void addTotalSellAmount(BigDecimal sellAmount) {
        this.totalSellAmount.updateAndGet(v -> v.add(sellAmount));
    }

    public AtomicReference<BigDecimal> getTotalBuyQuantity() {
        return totalBuyQuantity;
    }

    public void setTotalBuyQuantity(AtomicReference<BigDecimal> totalBuyQuantity) {
        this.totalBuyQuantity = totalBuyQuantity;
    }

    public void addTotalBuyQuantity(BigDecimal buyQuantity) {
        this.totalBuyQuantity.updateAndGet(v -> v.add(buyQuantity));
    }

    public AtomicReference<BigDecimal> getTotalSellQuantity() {
        return totalSellQuantity;
    }

    public void setTotalSellQuantity(AtomicReference<BigDecimal> totalSellQuantity) {
        this.totalSellQuantity = totalSellQuantity;
    }

    public void addTotalSellQuantity(BigDecimal sellQuantity) {
        this.totalSellQuantity.updateAndGet(v -> v.add(sellQuantity));
    }
}
