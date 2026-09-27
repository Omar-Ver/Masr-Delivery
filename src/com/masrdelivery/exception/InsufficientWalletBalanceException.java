package com.masrdelivery.exception;

public class InsufficientWalletBalanceException extends PlatformException {
    public InsufficientWalletBalanceException() {
        super("Transaction failed: Wallet balance is insufficient and may never go negative.");
    }
}
