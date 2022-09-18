package com.damncan.ordermatch.flink;

import com.damncan.ordermatch.bean.entity.PersistentStorage;
import com.damncan.ordermatch.bean.entity.SummaryEntity;
import com.damncan.ordermatch.bean.pojo.Summary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * This component is used to store the analysing result into a in-memory database, H2.
 *
 * @author Ian Zhong (damncan)
 * @since 18 September 2022
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class SummarySink extends RichSinkFunction<Summary> {

    @Autowired
    private PersistentStorage persistentStorage;

    private static EntityManager entityManager;

    private ObjectMapper mapper;

    public SummarySink() {
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        entityManager = Persistence.createEntityManagerFactory("FlinkSink").createEntityManager();
    }

    @Override
    public void invoke(Summary value, SinkFunction.Context context) throws Exception {
        System.out.println(mapper.writeValueAsString(value));

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        SummaryEntity summaryEntity = entityManager.find(SummaryEntity.class, 1);
        if (summaryEntity == null) {
            summaryEntity = new SummaryEntity();
        }

        if (value.getBuyOrSell().equals("B")) {
            persistentStorage.addTotalBuyOrder(value.getTotalOrder());
            persistentStorage.addTotalBuyAmount(value.getTotalAmount());
            persistentStorage.addTotalBuyQuantity(value.getTotalQuantity());
            System.out.println(String.format("%s Total matched order: %s, total matched amount: %s, total matched quantity: %s",
                    "[Buy]",
                    persistentStorage.getTotalBuyOrder(),
                    persistentStorage.getTotalBuyAmount(),
                    persistentStorage.getTotalBuyQuantity()));

            summaryEntity.setTotalBuyOrder(persistentStorage.getTotalBuyOrder().get());
            summaryEntity.setTotalBuyAmount(persistentStorage.getTotalBuyAmount().get());
            summaryEntity.setTotalBuyQuantity(persistentStorage.getTotalBuyQuantity().get());
        } else if (value.getBuyOrSell().equals("S")) {
            persistentStorage.addTotalSellOrder(value.getTotalOrder());
            persistentStorage.addTotalSellAmount(value.getTotalAmount());
            persistentStorage.addTotalSellQuantity(value.getTotalQuantity());
            System.out.println(String.format("%s Total matched order: %s, total matched amount: %s, total matched quantity: %s",
                    "[Sell]",
                    persistentStorage.getTotalSellOrder(),
                    persistentStorage.getTotalSellAmount(),
                    persistentStorage.getTotalSellQuantity()));

            summaryEntity.setTotalSellOrder(persistentStorage.getTotalSellOrder().get());
            summaryEntity.setTotalSellAmount(persistentStorage.getTotalSellAmount().get());
            summaryEntity.setTotalSellQuantity(persistentStorage.getTotalSellQuantity().get());
        }

        entityManager.merge(summaryEntity);

        transaction.commit();
    }
}
