package com.fintech.notification.service;

import com.fintech.platform.common.event.NotificationEvent;
import com.fintech.platform.common.event.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationEvent notificationEvent;
    private TransactionEvent transactionEvent;

    @BeforeEach
    void setUp() {
        notificationEvent = new NotificationEvent();
        notificationEvent.setTo("user@example.com");
        notificationEvent.setSubject("Test Notification");
        notificationEvent.setMessage("This is a test notification");
        notificationEvent.setType("TRANSACTION");

        transactionEvent = new TransactionEvent();
        transactionEvent.setTransactionId("txn-123");
        transactionEvent.setFromWalletId(1L);
        transactionEvent.setToWalletId(2L);
        transactionEvent.setAmount(new java.math.BigDecimal("100.00"));
        transactionEvent.setType("TRANSFER");
        transactionEvent.setStatus("COMPLETED");
    }

    @Test
    void shouldSendEmailNotificationSuccessfully() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.sendNotification(notificationEvent);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertArrayEquals(new String[]{"user@example.com"}, sentMessage.getTo());
        assertEquals("Test Notification", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("This is a test notification"));
    }

    @Test
    void shouldHandleTransactionEventSuccessfully() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.handleTransactionEvent(transactionEvent);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("Transaction Completed - txn-123", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("TRANSFER"));
        assertTrue(sentMessage.getText().contains("100.00"));
    }

    @Test
    void shouldHandleTransactionEventWithFailedStatus() {
        // Arrange
        transactionEvent.setStatus("FAILED");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.handleTransactionEvent(transactionEvent);

        // Assert
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("Transaction Failed - txn-123", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("FAILED"));
    }

    @Test
    void shouldSendNotificationWithoutEmailWhenMailSenderFails() {
        // Arrange
        doThrow(new RuntimeException("Mail server unavailable")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert - Should not throw exception, just log error
        assertDoesNotThrow(() -> notificationService.sendNotification(notificationEvent));
        
        // Verify mailSender was called
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldCreateNotificationEventWithCorrectType() {
        // Arrange & Act
        NotificationEvent event = new NotificationEvent();
        event.setTo("test@example.com");
        event.setSubject("Welcome");
        event.setMessage("Welcome to our platform");
        event.setType("WELCOME");

        // Assert
        assertEquals("test@example.com", event.getTo());
        assertEquals("Welcome", event.getSubject());
        assertEquals("Welcome to our platform", event.getMessage());
        assertEquals("WELCOME", event.getType());
    }
}
