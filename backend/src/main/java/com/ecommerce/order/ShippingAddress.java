package com.ecommerce.order;

import com.ecommerce.address.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * @Embeddable: um objeto de valor sem tabela própria. Seus campos são gravados
 * como colunas da tabela "orders" (shipping_*). Serve para agrupar dados que
 * fazem sentido juntos, sem criar mais uma entidade.
 */
@Embeddable
public class ShippingAddress {

    @Column(name = "shipping_recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "shipping_zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(name = "shipping_street", nullable = false, length = 200)
    private String street;

    @Column(name = "shipping_number", nullable = false, length = 20)
    private String number;

    @Column(name = "shipping_complement", length = 100)
    private String complement;

    @Column(name = "shipping_district", nullable = false, length = 100)
    private String district;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String city;

    @Column(name = "shipping_state", nullable = false, length = 2)
    private String state;

    protected ShippingAddress() {
    }

    public static ShippingAddress copyOf(Address address) {
        ShippingAddress shipping = new ShippingAddress();
        shipping.recipientName = address.getRecipientName();
        shipping.zipCode = address.getZipCode();
        shipping.street = address.getStreet();
        shipping.number = address.getNumber();
        shipping.complement = address.getComplement();
        shipping.district = address.getDistrict();
        shipping.city = address.getCity();
        shipping.state = address.getState();
        return shipping;
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
}
