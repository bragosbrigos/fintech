package com.fintech.wallet.controller;

import com.fintech.wallet.dto.DepositRequest;
import com.fintech.wallet.dto.WithdrawRequest;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Wallet> createWallet(@PathVariable String userId) {
        Wallet wallet = walletService.createWallet(userId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Wallet> getWalletByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Wallet> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> blockWallet(@PathVariable String id) {
        walletService.blockWallet(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockWallet(@PathVariable String id) {
        walletService.unblockWallet(id);
        return ResponseEntity.noContent().build();
    }
}
