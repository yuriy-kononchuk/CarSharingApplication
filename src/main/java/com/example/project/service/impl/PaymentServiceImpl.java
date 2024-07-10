package com.example.project.service.impl;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.payment.PaymentRequestDto;
import com.example.project.dto.payment.PaymentResponseDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.exception.IncorrectArgumentException;
import com.example.project.exception.PaymentProcessingException;
import com.example.project.mapper.PaymentMapper;
import com.example.project.model.Payment;
import com.example.project.model.Rental;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.RentalRepository;
import com.example.project.service.PaymentService;
import com.example.project.service.RentalService;
import com.example.project.service.TelegramNotificationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final Long QUANTITY = 1L;
    private static final String CURRENCY = "usd";
    private static final String NAME = "Car rental payment";
    private static final String PAYMENT_STATUS_PAID = "paid";
    private static final String SUCCESS_PATH = "/payments/success";
    private static final String CANCEL_PATH = "/payments/cancel";
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final TelegramNotificationService telegramNotificationService;
    @Value("${stripe.api.secret.key}")
    private String stripeApiKey;
    @Value("${base.url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public PaymentResponseDto createPaymentSession(PaymentRequestDto paymentRequestDto,
                                                   Long userId) {
        Set<Long> userRentalIds = rentalRepository.findByUserId(userId).stream()
                .map(Rental::getId)
                .collect(Collectors.toSet());
        if (!userRentalIds.contains(paymentRequestDto.rentalId()) || userRentalIds.isEmpty()) {
            throw new EntityNotFoundException(
                    "Denied! There's no user's rental with ID: " + paymentRequestDto.rentalId());
        }
        try {
            BigDecimal totalRentalPrice = rentalService.calculateRentalTotalPrice(
                    paymentRequestDto.rentalId(), paymentRequestDto.type()
            );

            URL successUrl = getUrl(SUCCESS_PATH);
            URL cancelUrl = getUrl(CANCEL_PATH);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setSuccessUrl(successUrl.toString())
                    .setCancelUrl(cancelUrl.toString())
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .addLineItem(createLineItem(totalRentalPrice))
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .build();
            Session session = Session.create(params);

            URL sessionUrl = new URL(session.getUrl());
            storePaymentWithSession(paymentRequestDto.rentalId(), sessionUrl, session.getId(),
                    totalRentalPrice, paymentRequestDto.type());

            return new PaymentResponseDto(session.getUrl(), session.getId());
        } catch (StripeException | MalformedURLException e) {
            throw new PaymentProcessingException("Can't create a payment session", e);
        }
    }

    @NotNull
    private URL getUrl(String path) throws MalformedURLException {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .build()
                .toUri()
                .toURL();
    }

    @Override
    public void handleSuccessfulPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        try {
            Session session = Session.retrieve(sessionId);
            if (session.getPaymentStatus().equals(PAYMENT_STATUS_PAID)) {
                payment.setStatus(Payment.Status.PAID);
                paymentRepository.save(payment);

                String message = String.format(
                        "Payment ID: %s is successful. Payed amount is %s USD. Status: %s",
                        payment.getId(), payment.getPayAmount(), payment.getStatus());
                telegramNotificationService.sendMessage(message);
            }
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error verifying payment 'paid' status", e);
        }
    }

    @Override
    public void handleCancelledPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
        payment.setStatus(Payment.Status.CANCELLED);
        paymentRepository.save(payment);
    }

    @Override
    public List<PaymentDto> findAllByUserId(Long userId, Pageable pageable) {
        if (userId != null) {
            List<Rental> rentalsByUserId = rentalRepository.findByUserId(userId);
            if (rentalsByUserId.isEmpty()) {
                throw new EntityNotFoundException(
                        "There's no rentals and payments by user ID: " + userId);
            }
            List<Payment> payments = rentalsByUserId.stream()
                    .flatMap(rental -> paymentRepository.findByRentalId(rental.getId()).stream())
                    .toList();
            return payments.stream()
                    .map(paymentMapper::toDto)
                    .toList();
        } else {
            throw new IncorrectArgumentException("User ID can't be null. Can't process payments");
        }
    }

    private void storePaymentWithSession(Long rentalId, URL sessionUrl, String sessionId,
                                         BigDecimal amount, Payment.Type type) {
        Payment payment = new Payment();
        payment.setRentalId(rentalId);
        payment.setSessionId(sessionId);
        payment.setSessionUrl(sessionUrl);
        payment.setPayAmount(amount);
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(type);
        paymentRepository.save(payment);
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(BigDecimal totalRentalPrice) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(CURRENCY)
                .setUnitAmount(totalRentalPrice.multiply(BigDecimal.valueOf(100)).longValueExact())
                .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(NAME)
                                .build()
                )
                .build();
    }

    public SessionCreateParams.LineItem createLineItem(BigDecimal totalRentalPrice) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(QUANTITY)
                .setPriceData(createPriceData(totalRentalPrice))
                .build();
    }
}
