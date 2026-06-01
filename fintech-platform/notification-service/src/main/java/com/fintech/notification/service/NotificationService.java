package com.fintech.notification.service;

import com.fintech.notification.dto.TransactionEvent;
import com.fintech.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendTransactionNotification(TransactionEvent event) {
        log.info("Processing notification for transaction: {}", event.getTransactionId());

        Notification notification = buildNotification(event);

        if ("COMPLETED".equals(event.getStatus())) {
            sendEmailNotification(
                    notification,
                    "Transação Realizada com Sucesso",
                    buildSuccessMessage(event)
            );
        } else if ("FAILED".equals(event.getStatus())) {
            sendEmailNotification(
                    notification,
                    "Transação Falhou",
                    buildFailureMessage(event)
            );
        }

        log.info("Notification sent for transaction: {}", event.getTransactionId());
    }

    private Notification buildNotification(TransactionEvent event) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .transactionId(event.getTransactionId())
                .type(event.getType())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void sendEmailNotification(Notification notification, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo("user@example.com"); // Em produção, buscar do User Service
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@fintech.com");

            mailSender.send(mailMessage);

            notification.setSent(true);
            notification.setSentAt(LocalDateTime.now());
            notification.setMessage(message);
            notification.setSubject(subject);

            log.info("Email sent successfully for transaction: {}", notification.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to send email for transaction {}: {}", notification.getTransactionId(), e.getMessage());
        }
    }

    private String buildSuccessMessage(TransactionEvent event) {
        return String.format(
                "Sua transação foi realizada com sucesso!\n\n" +
                "ID da Transação: %s\n" +
                "Tipo: %s\n" +
                "Valor: R$ %.2f\n" +
                "Descrição: %s\n\n" +
                "Obrigado por usar nossa plataforma!",
                event.getTransactionId(),
                event.getType(),
                event.getAmount(),
                event.getDescription()
        );
    }

    private String buildFailureMessage(TransactionEvent event) {
        return String.format(
                "Sua transação falhou.\n\n" +
                "ID da Transação: %s\n" +
                "Tipo: %s\n" +
                "Valor: R$ %.2f\n" +
                "Motivo: Verifique seu saldo ou tente novamente mais tarde.\n\n" +
                "Em caso de dúvidas, entre em contato com nosso suporte.",
                event.getTransactionId(),
                event.getType(),
                event.getAmount()
        );
    }
}
