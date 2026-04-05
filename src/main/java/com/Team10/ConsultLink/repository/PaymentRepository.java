package com.Team10.ConsultLink.repository;

import com.Team10.ConsultLink.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByClientUserId(String clientUserId);
}