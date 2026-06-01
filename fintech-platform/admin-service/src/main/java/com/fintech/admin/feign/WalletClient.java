package com.fintech.admin.feign;

import com.fintech.platform.common.dto.WalletDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "wallet-service", url = "${feign.client.wallet-service.url}")
public interface WalletClient {
    
    @GetMapping("/api/wallets")
    List<WalletDTO> getAllWallets();
    
    @GetMapping("/api/wallets/{id}")
    WalletDTO getWalletById(@PathVariable("id") String id);
    
    @GetMapping("/api/wallets/user/{userId}")
    WalletDTO getWalletByUserId(@PathVariable("userId") String userId);
    
    @GetMapping("/api/wallets/count")
    Long getTotalWalletCount();
    
    @GetMapping("/api/wallets/total-balance")
    java.math.BigDecimal getTotalBalance();
}
