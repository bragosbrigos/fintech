package com.fintech.wallet.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private String id;
    private String userId;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
