package com.masrdelivery.domain;
public class Address {

    private final String district;
    private final String detailline;

    public Address(String district, String detailline) {
        this.district = district;
        this.detailline = detailline;
    }

    public String getDistrict() {
        return district;
    }

    public String getDetailline() {
        return detailline;
    }
}
