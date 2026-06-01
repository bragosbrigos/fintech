package com.fintech.transaction.service;

import com.fintech.transaction.dto.CreateTransactionRequest;
import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.entity.Transaction;
import com.fintech.transaction.entity.TransactionStatus;
import com.fintech.transaction.entity.TransactionType;
import com.fintech.transaction.feign.WalletClient;
import com.fintech.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction: {}", request);

        // Validate wallets exist and are active
        var fromWallet = walletClient.getWalletById(request.getFromWalletId());
        var toWallet = walletClient.getWalletById(request.getToWalletId());

        if (!fromWallet.active() || !toWallet.active()) {
            throw new IllegalArgumentException("One or both wallets are inactive");
        }

        // Create transaction record in PENDING state
        Transaction transaction = Transaction.builder()
                .fromWalletId(request.getFromWalletId())
                .toWalletId(request.getToWalletId())
                .amount(request.getAmount())
                .type(TransactionType.valueOf(request.getType().toUpperCase()))
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Debit from source wallet
            walletClient.debitBalance(
                    request.getFromWalletId(),
                    new WalletClient.BalanceUpdateRequest(
                            request.getAmount(),
                            transaction.getTransactionId(),
                            "Debit for transaction: " + transaction.getTransactionId()
                    )
            );

            // Credit to destination wallet
            walletClient.creditBalance(
                    request.getToWalletId(),
                    new WalletClient.BalanceUpdateRequest(
                            request.getAmount(),
                            transaction.getTransactionId(),
                            "Credit for transaction: " + transaction.getTransactionId()
                    )
            );

            // Update transaction status to COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);

            // Send notification event
            sendNotificationEvent(transaction);

            log.info("Transaction completed successfully: {}", transaction.getTransactionId());

        } catch (Exception e) {
            log.error("Transaction failed: {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            transaction = transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWalletId(UUID walletId) {
        List<Transaction> transactions = transactionRepository.findByFromWalletIdOrToWalletId(walletId, walletId);
        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void sendNotificationEvent(Transaction transaction) {
        log.info("Sending notification event for transaction: {}", transaction.getTransactionId());
        kafkaTemplate.send("transaction-events", transaction.getTransactionId(), transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .fromWalletId(transaction.getFromWalletId())
                .toWalletId(transaction.getToWalletId())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .errorMessage(transaction.getErrorMessage())
                .build();
    }
}
