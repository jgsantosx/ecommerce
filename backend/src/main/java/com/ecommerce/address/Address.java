package com.ecommerce.address;

import com.ecommerce.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    /** Somente dígitos (8). A máscara 00000-000 é responsabilidade da interface. */
    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Address() {
    }

    public Address(User user) {
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public void apply(AddressRequest request) {
        this.recipientName = request.getRecipientName().trim();
        this.zipCode = request.getZipCode().replace("-", "");
        this.street = request.getStreet().trim();
        this.number = request.getNumber().trim();
        this.complement = request.getComplement() == null || request.getComplement().isBlank()
                ? null
                : request.getComplement().trim();
        this.district = request.getDistrict().trim();
        this.city = request.getCity().trim();
        this.state = request.getState().toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
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

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
