package com.reconciliation.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.reconciliation.exception.InvalidTransactionException;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class TransactionParserTest {

    private final TransactionParser parser =
            new TransactionParser();

    @Test
    void shouldParseValidTransaction() {

        String line =
                "TXN1001,1000.00,2026-09-12T10:30:15,SUCCESS";

        Transaction transaction =
                parser.parse(line);

        assertEquals(
                "TXN1001",
                transaction.getTransactionId());

        assertEquals(
                new BigDecimal("1000.00"),
                transaction.getAmount());

        assertEquals(
                LocalDateTime.of(2026, 9, 12, 10, 30, 15),
                transaction.getTransactionDateTime());

        assertEquals(
                TransactionStatus.SUCCESS,
                transaction.getStatus());
    }

    @Test
    void shouldRejectInvalidAmount() {

        String line =
                "TXN1001,ABC,2026-09-12T10:30:15,SUCCESS";

        assertThrows(
                InvalidTransactionException.class,
                () -> parser.parse(line));
    }

    @Test
    void shouldRejectInvalidDateTime() {

        String line =
                "TXN1001,1000.00,INVALID_DATE,SUCCESS";

        assertThrows(
                InvalidTransactionException.class,
                () -> parser.parse(line));
    }

    @Test
    void shouldRejectInvalidStatus() {

        String line =
                "TXN1001,1000.00,2026-09-12T10:30:15,COMPLETED";

        assertThrows(
                InvalidTransactionException.class,
                () -> parser.parse(line));
    }

    @Test
    void shouldRejectInvalidColumnCount() {

        String line =
                "TXN1001,1000.00,SUCCESS";

        assertThrows(
                InvalidTransactionException.class,
                () -> parser.parse(line));
    }
}