package com.masrdelivery.domain;

import com.masrdelivery.exception.InsufficientWalletBalanceException;
import com.masrdelivery.exception.PlatformException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {
    private final String id;
    private final String name;
    private final String mobileNumber;
    private final List<Address> addresses;
    private BigDecimal walletBalance;
    private int completedOrderCount;

    public Customer(String id, String name, String mobileNumber) {
        if (!mobileNumber.matches("^(010|011|012|015)\\d{8}$")) {
            throw new PlatformException("Invalid mobile number. Must be 11 digits starting with 010, 011, 012, or 015.");
        }

        this.id = id;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.addresses = new ArrayList<>();
        this.walletBalance = BigDecimal.ZERO;
        this.completedOrderCount = 0;


    }

    public void addAddresses(Address address) {
        this.addresses.add(address);
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public LoyaltyTier getLoyaltyTier() {
        return LoyaltyTier.fromOrderCount(this.completedOrderCount);
    }

    public void addFunds(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformException("Deposit amount must be greater than zero.");
        }
        this.walletBalance = this.walletBalance.add(amount);
    }


    public void deductFunds (BigDecimal amount){
        if (this.walletBalance.compareTo(amount)<0){
            throw new InsufficientWalletBalanceException();
        }
        this.walletBalance = this.walletBalance.subtract(amount);
    }

    public void incrementCompleteOrders(){
        this.completedOrderCount++;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }
}
