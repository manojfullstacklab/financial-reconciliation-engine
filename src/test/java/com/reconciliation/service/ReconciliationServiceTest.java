package com.reconciliation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class ReconciliationServiceTest {

    private final ReconciliationService service =
            new ReconciliationService();

    private final LocalDateTime transactionDateTime =
            LocalDateTime.of(2026, 9, 12, 10, 30, 15);

    @Test
    void shouldMatchTransactions() {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Transaction external =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(internal),
                        Arrays.asList(external));

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.MATCHED,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectAmountMismatch() {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Transaction external =
                createTransaction(
                        "TXN1001",
                        "1200.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(internal),
                        Arrays.asList(external));

        assertEquals(
                ReconciliationStatus.AMOUNT_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectStatusMismatch() {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Transaction external =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.FAILED);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(internal),
                        Arrays.asList(external));

        assertEquals(
                ReconciliationStatus.STATUS_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectDateTimeMismatch() {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Transaction external =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime.plusMinutes(5),
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(internal),
                        Arrays.asList(external));

        assertEquals(
                ReconciliationStatus.DATE_TIME_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectMissingExternalTransaction() {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(internal),
                        Arrays.asList());

        assertEquals(
                ReconciliationStatus.MISSING_EXTERNAL,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectMissingInternalTransaction() {

        Transaction external =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(),
                        Arrays.asList(external));

        assertEquals(
                ReconciliationStatus.MISSING_INTERNAL,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectDuplicateTransaction() {

        Transaction transaction1 =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Transaction transaction2 =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        List<ReconciliationResult> results =
                service.reconcile(
                        Arrays.asList(transaction1, transaction2),
                        Arrays.asList(transaction1));

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.DUPLICATE_TRANSACTION,
                results.get(0).getStatus());
    }
    
    @Test
    void shouldProcessLargeBatchSuccessfully() {

        List<Transaction> internalTransactions =
                new ArrayList<>();

        List<Transaction> externalTransactions =
                new ArrayList<>();

        for (int i = 1; i <= 2000; i++) {

            String transactionId =
                    String.format("TXN%04d", i);

            Transaction internal =
                    createTransaction(
                            transactionId,
                            "1000.00",
                            transactionDateTime,
                            TransactionStatus.SUCCESS);

            Transaction external =
                    createTransaction(
                            transactionId,
                            "1000.00",
                            transactionDateTime,
                            TransactionStatus.SUCCESS);

            internalTransactions.add(internal);
            externalTransactions.add(external);
        }

        List<ReconciliationResult> results =
                service.reconcile(
                        internalTransactions,
                        externalTransactions);

        assertEquals(2000, results.size());

        long matchedCount =
                results.stream()
                        .filter(result ->
                                result.getStatus()
                                        == ReconciliationStatus.MATCHED)
                        .count();

        assertEquals(2000, matchedCount);
    }

    private Transaction createTransaction(
            String transactionId,
            String amount,
            LocalDateTime dateTime,
            TransactionStatus status) {

        return new Transaction(
                transactionId,
                new BigDecimal(amount),
                dateTime,
                status);
    }
}