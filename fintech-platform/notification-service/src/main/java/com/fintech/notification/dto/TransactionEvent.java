package com.fintech.notification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {

    private String transactionId;
    
    private String fromWalletId;
    
    private String toWalletId;
    
    private Double amount;
    
    private String type;
    
    private String status;
    
    private String description;
}
