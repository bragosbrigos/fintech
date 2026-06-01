package com.fintech.wallet.listener;

import com.fintech.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedListener {

    private final WalletService walletService;

    @KafkaListener(topics = "user-created", groupId = "wallet-service-group")
    public void listenUserCreated(String userId) {
        log.info("Received event: user-created for userId: {}", userId);
        try {
            walletService.createWallet(userId);
            log.info("Wallet created successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Error creating wallet for user {}: {}", userId, e.getMessage());
        }
    }
}
