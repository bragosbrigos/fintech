package com.fintech.transaction.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

    private UUID fromWalletId;
    
    private UUID toWalletId;
    
    @NonNull
    private BigDecimal amount;
    
    private String type;
    
    private String description;
}
