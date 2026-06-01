package com.fintech.notification.listener;

import com.fintech.notification.dto.TransactionEvent;
import com.fintech.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
    public void listenTransactionEvents(TransactionEvent event) {
        log.info("Received transaction event: {}", event.getTransactionId());
        notificationService.sendTransactionNotification(event);
    }
}
