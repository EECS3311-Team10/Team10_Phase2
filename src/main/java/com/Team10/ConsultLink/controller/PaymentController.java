package com.Team10.ConsultLink.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.Team10.ConsultLink.model.Booking;
import com.Team10.ConsultLink.model.Payment;
import com.Team10.ConsultLink.model.PaymentMethod;
import com.Team10.ConsultLink.model.PaymentStatus;
import com.Team10.ConsultLink.repository.BookingRepository;
import com.Team10.ConsultLink.repository.PaymentRepository;
import com.Team10.ConsultLink.service.BankTransfer;
import com.Team10.ConsultLink.service.BankTransferStrategy;
import com.Team10.ConsultLink.service.CreditCard;
import com.Team10.ConsultLink.service.CreditCardStrategy;
import com.Team10.ConsultLink.service.DebitCard;
import com.Team10.ConsultLink.service.DebitCardStrategy;
import com.Team10.ConsultLink.service.PayPal;
import com.Team10.ConsultLink.service.PayPalStrategy;
import com.Team10.ConsultLink.service.PaymentStrategy;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @GetMapping("/client/{clientUserId}")
    public List<Payment> getPaymentsByClient(@PathVariable String clientUserId) {
        return paymentRepository.findByClientUserId(clientUserId);
    }

    @PostMapping("/{bookingId}")
    public Payment processPayment(@PathVariable Long bookingId,
                                  @RequestBody Map<String, String> data) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        String method = data.get("method");

        PaymentStrategy strategy = null;
        PaymentMethod paymentMethod = null;

        switch (method) {
            case "CREDIT":
                paymentMethod = new CreditCard(
                        data.get("cardNumber"),
                        data.get("holderName"),
                        YearMonth.parse(data.get("expiry")),
                        data.get("cvv")
                );
                strategy = new CreditCardStrategy((CreditCard) paymentMethod);
                break;

            case "DEBIT":
                paymentMethod = new DebitCard(
                        data.get("cardNumber"),
                        data.get("holderName"),
                        YearMonth.parse(data.get("expiry")),
                        data.get("cvv")
                );
                strategy = new DebitCardStrategy((DebitCard) paymentMethod);
                break;

            case "PAYPAL":
                paymentMethod = new PayPal(data.get("email"));
                strategy = new PayPalStrategy((PayPal) paymentMethod);
                break;

            case "BANK":
                paymentMethod = new BankTransfer(
                        data.get("accountNumber"),
                        data.get("address"),
                        data.get("accountName"),
                        data.get("routingNumber")
                );
                strategy = new BankTransferStrategy((BankTransfer) paymentMethod);
                break;

            default:
                throw new RuntimeException("Unknown payment method");
        }

        Payment payment = new Payment();
        payment.setPaymentID("PAY-" + System.currentTimeMillis());
        payment.setBookingID(booking.getBookingID());
        payment.setBookingDbId(booking.getId());
        payment.setClientUserId(booking.getClientUserId());
        payment.setServiceName(booking.getServiceName());
        payment.setPrice(booking.getPrice());
        payment.setMethod(method);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionID("TX-" + System.currentTimeMillis());

        if (paymentMethod.validate()) {
            strategy.pay(booking.getPrice());
            strategy.simPaymentProcessing();
            payment.setStatus(PaymentStatus.PAID.name());

            booking.setStatus("PAID");
            bookingRepository.save(booking);
        } else {
            payment.setStatus(PaymentStatus.FAILED.name());
        }

        return paymentRepository.save(payment);
    }
}