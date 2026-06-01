package com.fintech.wallet.service;

import com.fintech.platform.common.exception.ApiException;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUserId(100L);
        testWallet.setBalance(new BigDecimal("1000.00"));
        testWallet.setBlocked(false);
    }

    @Test
    void shouldCreateWalletSuccessfully() {
        // Arrange
        Long userId = 100L;
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.createWallet(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertFalse(result.getBlocked());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldThrowExceptionWhenWalletAlreadyExists() {
        // Arrange
        Long userId = 100L;
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(testWallet));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            walletService.createWallet(userId);
        });

        assertEquals("Wallet already exists for user", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void shouldGetWalletByIdSuccessfully() {
        // Arrange
        Long walletId = 1L;
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        // Act
        Wallet result = walletService.getWalletById(walletId);

        // Assert
        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(testWallet.getBalance(), result.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        // Arrange
        Long walletId = 999L;
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            walletService.getWalletById(walletId);
        });

        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void shouldDepositSuccessfully() {
        // Arrange
        Long walletId = 1L;
        BigDecimal amount = new BigDecimal("500.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.deposit(walletId, amount);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldThrowExceptionWhenDepositAmountIsInvalid() {
        // Arrange
        Long walletId = 1L;
        BigDecimal amount = new BigDecimal("-100.00");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            walletService.deposit(walletId, amount);
        });

        assertTrue(exception.getMessage().contains("Invalid deposit amount"));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void shouldDebitSuccessfully() {
        // Arrange
        Long walletId = 1L;
        BigDecimal amount = new BigDecimal("300.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        boolean result = walletService.debit(walletId, amount);

        // Assert
        assertTrue(result);
        assertEquals(new BigDecimal("700.00"), testWallet.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldReturnFalseWhenInsufficientBalanceForDebit() {
        // Arrange
        Long walletId = 1L;
        BigDecimal amount = new BigDecimal("2000.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        // Act
        boolean result = walletService.debit(walletId, amount);

        // Assert
        assertFalse(result);
        assertEquals(new BigDecimal("1000.00"), testWallet.getBalance());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void shouldThrowExceptionWhenDebitAmountIsInvalid() {
        // Arrange
        Long walletId = 1L;
        BigDecimal amount = new BigDecimal("-100.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            walletService.debit(walletId, amount);
        });

        assertTrue(exception.getMessage().contains("Invalid debit amount"));
    }

    @Test
    void shouldBlockWalletSuccessfully() {
        // Arrange
        Long walletId = 1L;
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.blockWallet(walletId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getBlocked());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldUnblockWalletSuccessfully() {
        // Arrange
        testWallet.setBlocked(true);
        Long walletId = 1L;
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.unblockWallet(walletId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getBlocked());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldGetWalletByUserIdSuccessfully() {
        // Arrange
        Long userId = 100L;
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(testWallet));

        // Act
        Wallet result = walletService.getWalletByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFoundByUserId() {
        // Arrange
        Long userId = 999L;
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            walletService.getWalletByUserId(userId);
        });

        assertEquals("Wallet not found for user", exception.getMessage());
    }
}
