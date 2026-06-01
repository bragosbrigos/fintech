package com.fintech.wallet.service;

import com.fintech.wallet.dto.DepositRequest;
import com.fintech.wallet.dto.WithdrawRequest;
import com.fintech.wallet.entity.Transaction;
import com.fintech.wallet.entity.TransactionType;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.TransactionRepository;
import com.fintech.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Wallet createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            log.warn("Wallet already exists for user: {}", userId);
            return walletRepository.findByUserId(userId).orElseThrow();
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully for user: {}", userId);
        return savedWallet;
    }

    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
    }

    @Transactional(readOnly = true)
    public Wallet getWalletById(String walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));
    }

    @Transactional
    public Wallet deposit(DepositRequest request) {
        Wallet wallet = getWalletById(request.getWalletId());
        
        if (!wallet.getActive()) {
            throw new IllegalStateException("Wallet is not active");
        }

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .fromWalletId(null)
                .toWalletId(wallet.getId())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .build();
        transactionRepository.save(transaction);

        // Publish event
        kafkaTemplate.send("transaction-created", transaction);

        log.info("Deposit successful: {} to wallet {}", request.getAmount(), wallet.getId());
        return wallet;
    }

    @Transactional
    public Wallet withdraw(WithdrawRequest request) {
        Wallet wallet = getWalletById(request.getWalletId());

        if (!wallet.getActive()) {
            throw new IllegalStateException("Wallet is not active");
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        // Record transaction
        Transaction transaction = Transaction.builder()
                .fromWalletId(wallet.getId())
                .toWalletId(null)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAWAL)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .build();
        transactionRepository.save(transaction);

        // Publish event
        kafkaTemplate.send("transaction-created", transaction);

        log.info("Withdrawal successful: {} from wallet {}", request.getAmount(), wallet.getId());
        return wallet;
    }

    @Transactional
    public void blockWallet(String walletId) {
        Wallet wallet = getWalletById(walletId);
        wallet.setActive(false);
        walletRepository.save(wallet);
        log.info("Wallet blocked: {}", walletId);
    }

    @Transactional
    public void unblockWallet(String walletId) {
        Wallet wallet = getWalletById(walletId);
        wallet.setActive(true);
        walletRepository.save(wallet);
        log.info("Wallet unblocked: {}", walletId);
    }
}
