package com.Team10.ConsultLink.model; 
import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
@Inheritance(strategy = InheritanceType.JOINED) // This handles CreditCard, PayPal, etc.
public abstract class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentType;

    // Default Constructor
    public PaymentMethod() {}

    // Methods from your UML
    public abstract boolean validate();
    
    // Getters and Setters
    public Long getId() { return id; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
}

/*package com.Team10.ConsultLink.model;

public abstract class PaymentMethod {
    private static int idCounter = 1; // static counter for generating unique payment method IDs
    protected int paymentMethodId;
    protected String paymentType;
    // Constructor
    public PaymentMethod(String paymentType) {
        this.paymentMethodId = idCounter++;
        this.paymentType = paymentType;
    }

    // Getters for payment method ID and type
    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public abstract boolean validate();
    public abstract String getPaymentDetails();

}
*/