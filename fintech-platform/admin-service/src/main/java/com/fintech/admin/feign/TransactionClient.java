package com.fintech.admin.feign;

import com.fintech.platform.common.dto.TransactionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "transaction-service", url = "${feign.client.transaction-service.url}")
public interface TransactionClient {
    
    @GetMapping("/api/transactions")
    List<TransactionDTO> getAllTransactions();
    
    @GetMapping("/api/transactions/{id}")
    TransactionDTO getTransactionById(@PathVariable("id") String id);
    
    @GetMapping("/api/transactions/wallet/{walletId}")
    List<TransactionDTO> getTransactionsByWalletId(@PathVariable("walletId") String walletId);
    
    @GetMapping("/api/transactions/count")
    Long getTotalTransactionCount();
    
    @GetMapping("/api/transactions/volume")
    java.math.BigDecimal getTotalTransactionVolume();
    
    @GetMapping("/api/transactions/status/count")
    TransactionStatusCountDTO getStatusCount();
    
    record TransactionStatusCountDTO(Long completed, Long failed, Long pending) {}
}
