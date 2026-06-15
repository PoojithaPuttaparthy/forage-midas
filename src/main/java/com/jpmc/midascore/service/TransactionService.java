package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRecordRepository recordRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
    }

    public void process(Transaction tx) {

        Long senderId = tx.getSenderId();
        Long recipientId = tx.getRecipientId();

        UserRecord sender = userRepository.findById(senderId).orElse(null);
        UserRecord recipient = userRepository.findById(recipientId).orElse(null);

        if (sender == null || recipient == null) return;
        if (sender.getBalance() < tx.getAmount()) return;


        Map response = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                tx,
                Map.class
        );

        double incentiveAmount = 0.0;
        if (response != null && response.get("amount") != null) {
            incentiveAmount = ((Number) response.get("amount")).doubleValue();
        }

        sender.setBalance((float)(sender.getBalance() - tx.getAmount()));

        recipient.setBalance((float)(recipient.getBalance() + tx.getAmount() + incentiveAmount));

        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord();
        record.setSender(sender);
        record.setRecipient(recipient);
        record.setAmount(tx.getAmount());
        record.setIncentive(incentiveAmount);

        recordRepository.save(record);
        System.out.println("Sender: " + sender.getName() + " balance: " + sender.getBalance());
        System.out.println("Recipient: " + recipient.getName() + " balance: " + recipient.getBalance());
    }
}