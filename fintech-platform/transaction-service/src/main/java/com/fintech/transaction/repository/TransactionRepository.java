package com.fintech.transaction.repository;

import com.fintech.transaction.entity.Transaction;
import com.fintech.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByFromWalletId(UUID walletId);
    
    List<Transaction> findByToWalletId(UUID walletId);
    
    List<Transaction> findByFromWalletIdOrToWalletId(UUID fromWalletId, UUID toWalletId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByFromWalletIdAndStatus(UUID walletId, TransactionStatus status);
}
