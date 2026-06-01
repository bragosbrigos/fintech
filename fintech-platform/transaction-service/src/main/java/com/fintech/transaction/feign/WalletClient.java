package com.fintech.transaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "wallet-service", path = "/api/wallets")
public interface WalletClient {

    @GetMapping("/{id}")
    WalletResponse getWalletById(@PathVariable UUID id);

    @PutMapping("/{id}/credit")
    WalletResponse creditBalance(@PathVariable UUID id, @RequestBody BalanceUpdateRequest request);

    @PutMapping("/{id}/debit")
    WalletResponse debitBalance(@PathVariable UUID id, @RequestBody BalanceUpdateRequest request);

    record WalletResponse(UUID id, UUID userId, BigDecimal balance, boolean active) {}

    record BalanceUpdateRequest(BigDecimal amount, String transactionId, String description) {}
}
