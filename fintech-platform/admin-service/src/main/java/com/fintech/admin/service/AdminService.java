package com.fintech.admin.service;

import com.fintech.admin.dto.DashboardMetricsDTO;
import com.fintech.admin.dto.TransactionReportDTO;
import com.fintech.admin.feign.TransactionClient;
import com.fintech.admin.feign.UserClient;
import com.fintech.admin.feign.WalletClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserClient userClient;
    private final WalletClient walletClient;
    private final TransactionClient transactionClient;

    @Cacheable(value = "dashboard-metrics", ttl = 300)
    public DashboardMetricsDTO getDashboardMetrics() {
        log.info("Fetching dashboard metrics");

        Long totalUsers = userClient.getTotalUserCount();
        Long totalWallets = walletClient.getTotalWalletCount();
        BigDecimal totalBalance = walletClient.getTotalBalance();
        
        Long totalTransactions = transactionClient.getTotalTransactionCount();
        BigDecimal totalVolume = transactionClient.getTotalTransactionVolume();
        
        var statusCount = transactionClient.getStatusCount();
        
        return DashboardMetricsDTO.builder()
                .totalUsers(totalUsers)
                .totalWallets(totalWallets)
                .totalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO)
                .totalTransactions(totalTransactions)
                .totalTransactionVolume(totalVolume != null ? totalVolume : BigDecimal.ZERO)
                .completedTransactions(statusCount.completed())
                .failedTransactions(statusCount.failed())
                .pendingTransactions(statusCount.pending())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public List<TransactionReportDTO> getTransactionReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating transaction report from {} to {}", startDate, endDate);
        
        List<TransactionReportDTO> reports = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // Em produção, isso viria de uma consulta agregada no banco de dados
            reports.add(TransactionReportDTO.builder()
                    .date(currentDate)
                    .transactionCount(0L)
                    .totalVolume(BigDecimal.ZERO)
                    .averageTransactionValue(BigDecimal.ZERO)
                    .completedCount(0L)
                    .failedCount(0L)
                    .pendingCount(0L)
                    .build());
            currentDate = currentDate.plusDays(1);
        }
        
        return reports;
    }

    public void refreshCache() {
        log.info("Refreshing admin cache");
        // Cache will be refreshed on next call to cached methods
    }
}
