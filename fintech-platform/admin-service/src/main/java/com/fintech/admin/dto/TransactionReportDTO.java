package com.fintech.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TransactionReportDTO {
    
    private LocalDate date;
    private Long transactionCount;
    private BigDecimal totalVolume;
    private BigDecimal averageTransactionValue;
    private Long completedCount;
    private Long failedCount;
    private Long pendingCount;
}
