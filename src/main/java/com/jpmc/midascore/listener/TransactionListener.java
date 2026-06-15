package com.jpmc.midascore.listener;

import com.jpmc.midascore.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.jpmc.midascore.foundation.Transaction;

@Component

public class TransactionListener {

    private final TransactionService service;

    public TransactionListener(TransactionService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        service.process(transaction);
    }
}