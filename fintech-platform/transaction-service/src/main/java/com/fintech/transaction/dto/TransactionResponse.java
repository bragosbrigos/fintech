package com.fintech.transaction.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;
    
    private String transactionId;
    
    private UUID fromWalletId;
    
    private UUID toWalletId;
    
    private BigDecimal amount;
    
    private String type;
    
    private String status;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    private String errorMessage;
}
