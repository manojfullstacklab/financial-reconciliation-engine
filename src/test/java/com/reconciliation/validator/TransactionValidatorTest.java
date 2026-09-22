package com.reconciliation.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.reconciliation.exception.InvalidTransactionException;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class TransactionValidatorTest {

    private final TransactionValidator validator =
            new TransactionValidator();

    private final LocalDateTime dateTime =
            LocalDateTime.of(2026, 9, 12, 10, 30, 15);

    @Test
    void shouldAcceptValidTransaction() {

        Transaction transaction =
                new Transaction(
                        "TXN1001",
                        new BigDecimal("1000.00"),
                        dateTime,
                        TransactionStatus.SUCCESS);

        assertDoesNotThrow(
                () -> validator.validate(transaction));
    }

    @Test
    void shouldRejectNullTransaction() {

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(null));
    }

    @Test
    void shouldRejectEmptyTransactionId() {

        Transaction transaction =
                new Transaction(
                        "",
                        new BigDecimal("1000.00"),
                        dateTime,
                        TransactionStatus.SUCCESS);

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(transaction));
    }

    @Test
    void shouldRejectZeroAmount() {

        Transaction transaction =
                new Transaction(
                        "TXN1001",
                        BigDecimal.ZERO,
                        dateTime,
                        TransactionStatus.SUCCESS);

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(transaction));
    }

    @Test
    void shouldRejectNegativeAmount() {

        Transaction transaction =
                new Transaction(
                        "TXN1001",
                        new BigDecimal("-100.00"),
                        dateTime,
                        TransactionStatus.SUCCESS);

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(transaction));
    }

    @Test
    void shouldRejectNullDateTime() {

        Transaction transaction =
                new Transaction(
                        "TXN1001",
                        new BigDecimal("1000.00"),
                        null,
                        TransactionStatus.SUCCESS);

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(transaction));
    }

    @Test
    void shouldRejectNullStatus() {

        Transaction transaction =
                new Transaction(
                        "TXN1001",
                        new BigDecimal("1000.00"),
                        dateTime,
                        null);

        assertThrows(
                InvalidTransactionException.class,
                () -> validator.validate(transaction));
    }
}