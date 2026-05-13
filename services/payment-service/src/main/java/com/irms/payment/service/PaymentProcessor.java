package com.irms.payment.service;

import com.irms.payment.domain.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentProcessor {

    public String process(Payment payment) {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
