package com.fintech.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DashboardMetricsDTO {
    
    private Long totalUsers;
    private Long totalWallets;
    private BigDecimal totalBalance;
    private Long totalTransactions;
    private BigDecimal totalTransactionVolume;
    private Long completedTransactions;
    private Long failedTransactions;
    private Long pendingTransactions;
    private LocalDateTime lastUpdated;
}
