package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.payment.PaymentRequestDto;
import com.example.project.dto.payment.PaymentResponseDto;
import com.example.project.exception.EntityNotFoundException;
import com.example.project.exception.IncorrectArgumentException;
import com.example.project.mapper.PaymentMapper;
import com.example.project.model.Payment;
import com.example.project.model.Rental;
import com.example.project.model.User;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.RentalRepository;
import com.example.project.service.impl.PaymentServiceImpl;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RentalService rentalService;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private TelegramNotificationService telegramNotificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;
    @Value("${stripe.api.secret.key}")
    private String stripeApiKey;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    public void setup() {
        Stripe.apiKey = stripeApiKey;
        ReflectionTestUtils.setField(paymentService, "baseUrl", baseUrl);
        paymentService.init();
    }

    @Test
    @DisplayName("Verify createPaymentSession() creates Payment Session Successfully")
    void createPaymentSession_ValidRequestDtoAndUserId_ReturnsPaymentResponseDto()
            throws StripeException, MalformedURLException {
        Long userId = 1L;
        Long rentalId = 1L;
        User user = new User();
        user.setId(userId);
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(rentalId, Payment.Type.PAYMENT);

        Rental rental = Rental.builder()
                .id(rentalId)
                .user(user)
                .build();
        Set<Rental> rentals = Set.of(rental);
        user.setRentals(rentals);

        when(rentalRepository.findByUserId(userId)).thenReturn(List.of(rental));

        BigDecimal totalRentalPrice = BigDecimal.valueOf(100.00);
        when(rentalService.calculateRentalTotalPrice(rentalId, Payment.Type.PAYMENT))
                .thenReturn(totalRentalPrice);

        URL successUrl = new URL(baseUrl + "/payments/success");
        URL cancelUrl = new URL(baseUrl + "/payments/cancel");

        Session session = mock(Session.class);
        when(session.getUrl()).thenReturn("http://session.url");
        when(session.getId()).thenReturn("session_id");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setSuccessUrl(successUrl.toString())
                    .setCancelUrl(cancelUrl.toString())
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .addLineItem(paymentService.createLineItem(totalRentalPrice))
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .build();
            mockedSession.when(() -> Session.create(
                    any(SessionCreateParams.class))).thenReturn(session);

            PaymentResponseDto actualResponseDto = paymentService
                    .createPaymentSession(paymentRequestDto, userId);

            assertNotNull(actualResponseDto);
            assertEquals("http://session.url", actualResponseDto.sessionUrl());
            assertEquals("session_id", actualResponseDto.sessionId());

            verify(rentalRepository, times(1)).findByUserId(userId);
            verify(rentalService, times(1))
                    .calculateRentalTotalPrice(rentalId, Payment.Type.PAYMENT);
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verifyNoMoreInteractions(rentalRepository, rentalService, paymentRepository);
        }
    }

    @Test
    @DisplayName("Verify createPaymentSession() method throws Exception with invalid user Id")
    public void createPaymentSession_InvalidUserId_ThrowsException() {
        Long userId = 100L;
        Long rentalId = 1L;
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(rentalId, Payment.Type.PAYMENT);

        when(rentalRepository.findByUserId(userId)).thenReturn(List.of());

        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                paymentService.createPaymentSession(paymentRequestDto, userId));

        assertEquals("Denied! There's no user's rental with ID: " + rentalId,
                exception.getMessage());
        verify(rentalRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(rentalRepository);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    @DisplayName("Verify to handle successful payment")
    void handleSuccessfulPayment_Success() {
        String sessionId = "session_id";
        Payment payment = new Payment();
        payment.setSessionId(sessionId);
        payment.setStatus(Payment.Status.PENDING);
        payment.setPayAmount(BigDecimal.valueOf(100.00));

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn("paid");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId)).thenReturn(session);

            paymentService.handleSuccessfulPayment(sessionId);
        }

        String expectedMessage = String.format(
                "Payment ID: %s is successful. Payed amount is %s USD. Status: %s",
                payment.getId(), payment.getPayAmount(), payment.getStatus());

        assertEquals(Payment.Status.PAID, payment.getStatus());
        verify(telegramNotificationService).sendMessage(expectedMessage);
        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verify(paymentRepository, times(1)).save(payment);
        verify(telegramNotificationService, times(1)).sendMessage(expectedMessage);
        verifyNoMoreInteractions(paymentRepository, telegramNotificationService);
    }

    @Test
    @DisplayName("Verify handleSuccessfulPayment() method throws exception with invalid session Id")
    void handleSuccessfulPayment_InvalidSessionId_ThrowsException() {
        String sessionId = "session_id";
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleSuccessfulPayment(sessionId));

        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Verify to handle cancelled payment")
    void handleCancelledPayment_Success() {
        String sessionId = "session_id";
        Payment payment = new Payment();
        payment.setSessionId(sessionId);
        payment.setStatus(Payment.Status.PENDING);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        paymentService.handleCancelledPayment(sessionId);

        assertEquals(Payment.Status.CANCELLED, payment.getStatus());
        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Verify handleCancelledPayment() method throws exception with invalid session Id")
    void handleCancelledPayment_InvalidSessionId_ThrowsException() {
        String sessionId = "session_id";
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.handleCancelledPayment(sessionId));

        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository, times(1)).findBySessionId(sessionId);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Verify findAllByUserId() method returns payments successfully")
    void findAllByUserId_ValidUserId_ReturnsPaymentDtos() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Rental rental1 = Rental.builder()
                .id(1L)
                .user(user)
                .build();
        Rental rental2 = Rental.builder()
                .id(2L)
                .user(user)
                .build();
        Set<Rental> rentals = Set.of(rental1, rental2);
        user.setRentals(rentals);

        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setRentalId(1L);
        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setRentalId(2L);

        PaymentDto paymentDto1 = new PaymentDto();
        paymentDto1.setId(payment1.getId());
        paymentDto1.setRentalId(payment1.getRentalId());
        PaymentDto paymentDto2 = new PaymentDto();
        paymentDto2.setId(payment2.getId());
        paymentDto2.setRentalId(payment2.getRentalId());

        List<Rental> expectedRentals = List.of(rental1, rental2);

        when(rentalRepository.findByUserId(userId)).thenReturn(expectedRentals);
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.of(payment1));
        when(paymentRepository.findByRentalId(2L)).thenReturn(Optional.of(payment2));
        when(paymentMapper.toDto(payment1)).thenReturn(paymentDto1);
        when(paymentMapper.toDto(payment2)).thenReturn(paymentDto2);

        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentDto> expectedPaymentDtos = List.of(paymentDto1, paymentDto2);
        List<PaymentDto> actualPaymentDtos = paymentService.findAllByUserId(userId, pageable);

        assertEquals(expectedPaymentDtos, actualPaymentDtos);
        assertEquals(actualPaymentDtos.size(), 2);
        assertEquals(paymentDto1.getId(), actualPaymentDtos.get(0).getId());
        assertEquals(paymentDto1.getRentalId(), actualPaymentDtos.get(0).getRentalId());
        assertEquals(paymentDto2.getId(), actualPaymentDtos.get(1).getId());
        assertEquals(paymentDto2.getRentalId(), actualPaymentDtos.get(1).getRentalId());
        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(paymentRepository, times(1)).findByRentalId(1L);
        verify(paymentRepository, times(1)).findByRentalId(2L);
        verify(paymentMapper, times(1)).toDto(payment1);
        verify(paymentMapper, times(1)).toDto(payment2);
        verifyNoMoreInteractions(rentalRepository, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify findAllByUserId() method throws exception for invalid user ID")
    void findAllByUserId_InvalidUserId_ThrowsException() {
        Long userId = 100L;
        when(rentalRepository.findByUserId(userId)).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.findAllByUserId(userId, pageable));

        assertEquals("There's no rentals and payments by user ID: " + userId,
                exception.getMessage());
        verify(rentalRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(rentalRepository, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify findAllByUserId() method throws Exception whith no user's rentals")
    void findAllByUserId_UserWithNoRentals_Exception() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(rentalRepository.findByUserId(userId)).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 10);
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> paymentService.findAllByUserId(userId, pageable));

        assertEquals("There's no rentals and payments by user ID: " + userId,
                exception.getMessage());
        verify(rentalRepository, times(1)).findByUserId(userId);
        verifyNoMoreInteractions(rentalRepository, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify findAllByUserId() method throws exception for user ID is null")
    void findAllByUserId_NullUserId_ThrowsException() {
        Long userId = null;

        Pageable pageable = PageRequest.of(0, 10);
        Exception exception = assertThrows(IncorrectArgumentException.class,
                () -> paymentService.findAllByUserId(userId, pageable));

        assertEquals("User ID can't be null. Can't process payments",
                exception.getMessage());
        verifyNoMoreInteractions(rentalRepository, paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("Verify findAllByUserId() method returns empty list when no payments found")
    void findAllByUserId_NoPaymentsFound_ReturnsEmptyList() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Rental rental1 = Rental.builder()
                .id(1L)
                .user(user)
                .build();
        Rental rental2 = Rental.builder()
                .id(2L)
                .user(user)
                .build();
        List<Rental> expectedRentals = List.of(rental1, rental2);

        when(rentalRepository.findByUserId(userId)).thenReturn(expectedRentals);
        when(paymentRepository.findByRentalId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.findByRentalId(2L)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentDto> actualPaymentDtos = paymentService.findAllByUserId(userId, pageable);

        assertNotNull(actualPaymentDtos);
        assertEquals(0, actualPaymentDtos.size());
        verify(rentalRepository, times(1)).findByUserId(userId);
        verify(paymentRepository, times(1)).findByRentalId(1L);
        verify(paymentRepository, times(1)).findByRentalId(2L);
        verifyNoMoreInteractions(rentalRepository, paymentRepository, paymentMapper);
    }
}
