package com.example.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.project.dto.payment.PaymentDto;
import com.example.project.dto.payment.PaymentRequestDto;
import com.example.project.dto.payment.PaymentResponseDto;
import com.example.project.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = {
            "classpath:database/payments/delete-test-payments-from-payments-table.sql",
            "classpath:database/users/add-test-users-cars-set.sql",
            "classpath:database/payments/add-test-payments-to-payments-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete-test-payments-from-payments-table.sql",
            "classpath:database/users/delete-test-users-cars-set.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Customer gets own payments only is successful")
    void getAllPayments_ValidAuthenticatedUserCustomerAndAnyUserId_Success() throws Exception {
        PaymentDto paymentDto1 = new PaymentDto();
        paymentDto1.setId(1L);
        paymentDto1.setRentalId(1L);
        paymentDto1.setStatus(Payment.Status.PAID);
        paymentDto1.setType(Payment.Type.PAYMENT);
        paymentDto1.setSessionUrl(new URL("http://example.com/session/123"));
        paymentDto1.setSessionId("session123");
        paymentDto1.setPayAmount(new BigDecimal("100.00"));

        PaymentDto paymentDto2 = new PaymentDto();
        paymentDto2.setId(2L);
        paymentDto2.setRentalId(2L);
        paymentDto2.setStatus(Payment.Status.PENDING);
        paymentDto2.setType(Payment.Type.FINE);
        paymentDto2.setSessionUrl(new URL("http://example.com/session/456"));
        paymentDto2.setSessionId("session456");
        paymentDto2.setPayAmount(new BigDecimal("50.00"));

        List<PaymentDto> expected = new ArrayList<>();
        expected.add(paymentDto1);
        expected.add(paymentDto2);

        Long userId = 100L;

        MvcResult result = mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        PaymentDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), PaymentDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("manager@test.com")
    @Test
    @Sql(scripts = {
            "classpath:database/users/add-test-users-cars-set.sql",
            "classpath:database/payments/add-test-payments-to-payments-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete-test-payments-from-payments-table.sql",
            "classpath:database/users/delete-test-users-cars-set.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Manager gets payments of any user by its ID is successful")
    void getAllPayments_ValidAuthenticatedManagerAndIdAnotherUser_Success() throws Exception {
        PaymentDto paymentDto1 = new PaymentDto();
        paymentDto1.setId(1L);
        paymentDto1.setRentalId(1L);
        paymentDto1.setStatus(Payment.Status.PAID);
        paymentDto1.setType(Payment.Type.PAYMENT);
        paymentDto1.setSessionUrl(new URL("http://example.com/session/123"));
        paymentDto1.setSessionId("session123");
        paymentDto1.setPayAmount(new BigDecimal("100.00"));

        PaymentDto paymentDto2 = new PaymentDto();
        paymentDto2.setId(2L);
        paymentDto2.setRentalId(2L);
        paymentDto2.setStatus(Payment.Status.PENDING);
        paymentDto2.setType(Payment.Type.FINE);
        paymentDto2.setSessionUrl(new URL("http://example.com/session/456"));
        paymentDto2.setSessionId("session456");
        paymentDto2.setPayAmount(new BigDecimal("50.00"));

        List<PaymentDto> expected = new ArrayList<>();
        expected.add(paymentDto1);
        expected.add(paymentDto2);

        Long userId = 5L;

        MvcResult result = mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        PaymentDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), PaymentDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("manager@test.com")
    @Test
    @Sql(scripts = {
            "classpath:database/users/delete-test-users-cars-set.sql",
            "classpath:database/users/add-test-users-cars-set.sql",
            "classpath:database/payments/add-test-payments-to-payments-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete-test-payments-from-payments-table.sql",
            "classpath:database/users/delete-test-users-cars-set.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Manager gets owned payments of any user by own user ID is successful")
    void getAllPayments_ValidAuthenticatedManagerAndIdThisUser_Success() throws Exception {
        PaymentDto paymentDto1 = new PaymentDto();
        paymentDto1.setId(3L);
        paymentDto1.setRentalId(4L);
        paymentDto1.setStatus(Payment.Status.PAID);
        paymentDto1.setType(Payment.Type.PAYMENT);
        paymentDto1.setSessionUrl(new URL("http://example.com/session/777"));
        paymentDto1.setSessionId("session777");
        paymentDto1.setPayAmount(new BigDecimal("100.00"));

        PaymentDto paymentDto2 = new PaymentDto();
        paymentDto2.setId(4L);
        paymentDto2.setRentalId(5L);
        paymentDto2.setStatus(Payment.Status.PENDING);
        paymentDto2.setType(Payment.Type.FINE);
        paymentDto2.setSessionUrl(new URL("http://example.com/session/888"));
        paymentDto2.setSessionId("session888");
        paymentDto2.setPayAmount(new BigDecimal("50.00"));

        List<PaymentDto> expected = new ArrayList<>();
        expected.add(paymentDto1);
        expected.add(paymentDto2);

        Long userId = 6L;

        MvcResult result = mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        PaymentDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), PaymentDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails("manager@test.com")
    @Test
    @Sql(scripts = {
            "classpath:database/users/add-test-users-cars-set.sql",
            "classpath:database/payments/add-test-payments-to-payments-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/payments/delete-test-payments-from-payments-table.sql",
            "classpath:database/users/delete-test-users-cars-set.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Manager gets payments for invalid user ID is Not Found")
    void getAllPayments_InvalidUserId_ReturnsNotFound() throws Exception {
        Long invalidUserId = 100L;

        mockMvc.perform(get("/payments")
                        .param("userId", String.valueOf(invalidUserId)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create payment session is successful")
    void createPaymentSession_ValidRequestDtoAndNotActiveRental_Success() throws Exception {
        PaymentRequestDto requestDto = new PaymentRequestDto(3L, Payment.Type.PAYMENT);

        // Mock the Stripe Session creation with required parameters
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_test_123");
        when(session.getUrl()).thenReturn("https://checkout.stripe.com/c/pay/test123");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(
                    any(SessionCreateParams.class))).thenReturn(session);

            MvcResult result = mockMvc.perform(post("/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            PaymentResponseDto actual = objectMapper.readValue(
                    result.getResponse().getContentAsString(), PaymentResponseDto.class);

            assertNotNull(actual);
            assertNotNull(actual.sessionId());
            assertNotNull(actual.sessionUrl());
            assertTrue(actual.sessionId().startsWith("cs_"));
            assertTrue(actual.sessionUrl().startsWith("https://checkout.stripe.com/c/pay/"));
        }
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-users-cars-set.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create payment session with active rental is Bad Request")
    void createPaymentSession_ValidRequestDtoAndActiveRental_ReturnsBadRequest() throws Exception {
        PaymentRequestDto requestDto = new PaymentRequestDto(1L, Payment.Type.PAYMENT);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @WithUserDetails("testuser@test.com")
    @Test
    @Sql(scripts = "classpath:database/users/add-test-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/users/delete-test-user.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create payment session with active rental is Bad Request")
    void createPaymentSession_InValidRentalId_ReturnsNorFound() throws Exception {
        PaymentRequestDto requestDto = new PaymentRequestDto(100L, Payment.Type.PAYMENT);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
}
