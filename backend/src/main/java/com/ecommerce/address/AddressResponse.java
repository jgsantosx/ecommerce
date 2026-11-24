package com.ecommerce.address;

import java.time.LocalDateTime;

public class AddressResponse {

    private Long id;
    private String recipientName;
    private String zipCode;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private boolean defaultAddress;
    private LocalDateTime createdAt;

    public AddressResponse() {
    }

    public static AddressResponse fromEntity(Address address) {
        AddressResponse response = new AddressResponse();
        response.id = address.getId();
        response.recipientName = address.getRecipientName();
        response.zipCode = address.getZipCode();
        response.street = address.getStreet();
        response.number = address.getNumber();
        response.complement = address.getComplement();
        response.district = address.getDistrict();
        response.city = address.getCity();
        response.state = address.getState();
        response.defaultAddress = address.isDefaultAddress();
        response.createdAt = address.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
