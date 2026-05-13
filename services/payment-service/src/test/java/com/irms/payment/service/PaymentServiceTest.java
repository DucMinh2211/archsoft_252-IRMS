package com.irms.payment.service;

import com.irms.payment.domain.Payment;
import com.irms.payment.domain.PaymentMethod;
import com.irms.payment.domain.PaymentStatus;
import com.irms.payment.infrastructure.client.OrderCompletionClient;
import com.irms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderCompletionClient orderCompletionClient;

    @Test
    void processPayment_ShouldCompletePaymentAndNotifyOrderService() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(new BigDecimal("25.50"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .build();
        payment.setId(paymentId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentService paymentService = new PaymentService(paymentRepository, orderCompletionClient, new PaymentProcessor(), new PaymentMapper());

        var response = paymentService.processPayment(paymentId);

        assertEquals(PaymentStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getTransactionId());
        assertTrue(response.getTransactionId().startsWith("TXN-"));
        verify(orderCompletionClient).updateOrderStatusToCompleted(orderId);
    }
}
