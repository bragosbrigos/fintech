package com.fintech.transaction.service;

import com.fintech.platform.common.event.TransactionEvent;
import com.fintech.transaction.dto.CreateTransactionRequest;
import com.fintech.transaction.entity.Transaction;
import com.fintech.transaction.entity.TransactionStatus;
import com.fintech.transaction.entity.TransactionType;
import com.fintech.transaction.feign.WalletClient;
import com.fintech.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private CreateTransactionRequest request;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "transactionIdempotencyWindow", 300);

        request = new CreateTransactionRequest();
        request.setFromWalletId(1L);
        request.setToWalletId(2L);
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.TRANSFER);
        request.setDescription("Test transfer");

        transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setFromWalletId(1L);
        transaction.setToWalletId(2L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("Test transfer");
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        // Arrange
        when(walletClient.getWalletById(request.getFromWalletId())).thenReturn(createWalletDTO(1L, new BigDecimal("500.00")));
        when(walletClient.getWalletById(request.getToWalletId())).thenReturn(createWalletDTO(2L, new BigDecimal("200.00")));
        when(walletClient.debit(anyLong(), any())).thenReturn(true);
        when(walletClient.credit(anyLong(), any())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        Transaction result = transactionService.createTransaction(request);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(walletClient).debit(eq(1L), eq(new BigDecimal("100.00")));
        verify(walletClient).credit(eq(2L), eq(new BigDecimal("100.00")));
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transactions"), any(TransactionEvent.class));
    }

    @Test
    void shouldFailWhenInsufficientBalance() {
        // Arrange
        when(walletClient.getWalletById(request.getFromWalletId())).thenReturn(createWalletDTO(1L, new BigDecimal("50.00")));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(walletClient, never()).debit(anyLong(), any());
        verify(walletClient, never()).credit(anyLong(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldHandleIdempotencyForDuplicateRequests() {
        // Arrange
        String idempotencyKey = UUID.nameUUIDFromBytes(
            (request.getFromWalletId() + ":" + request.getToWalletId() + ":" + 
             request.getAmount() + ":" + request.getType()).getBytes()
        ).toString();

        Transaction existingTransaction = new Transaction();
        existingTransaction.setIdempotencyKey(idempotencyKey);
        existingTransaction.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findByIdempotencyKeyAndCreatedAtAfter(
            idempotencyKey, LocalDateTime.now().minusSeconds(300)
        )).thenReturn(Optional.of(existingTransaction));

        // Act
        Transaction result = transactionService.createTransaction(request);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        verify(walletClient, never()).debit(anyLong(), any());
        verify(walletClient, never()).credit(anyLong(), any());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldGetTransactionHistoryByWalletId() {
        // Arrange
        Long walletId = 1L;
        when(transactionRepository.findByFromWalletIdOrToWalletIdOrderByCreatedAtDesc(walletId, walletId))
            .thenReturn(List.of(transaction));

        // Act
        List<Transaction> result = transactionService.getTransactionHistory(walletId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(walletId, result.get(0).getFromWalletId());
    }

    @Test
    void shouldRollbackTransactionOnCreditFailure() {
        // Arrange
        when(walletClient.getWalletById(request.getFromWalletId())).thenReturn(createWalletDTO(1L, new BigDecimal("500.00")));
        when(walletClient.getWalletById(request.getToWalletId())).thenReturn(createWalletDTO(2L, new BigDecimal("200.00")));
        when(walletClient.debit(anyLong(), any())).thenReturn(true);
        when(walletClient.credit(anyLong(), any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // Act
        Transaction result = transactionService.createTransaction(request);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(walletClient).rollbackDebit(eq(1L), eq(new BigDecimal("100.00")));
    }

    private Object createWalletDTO(Long id, BigDecimal balance) {
        return new Object() {
            public Long getId() { return id; }
            public BigDecimal getBalance() { return balance; }
            public Boolean getBlocked() { return false; }
        };
    }
}
