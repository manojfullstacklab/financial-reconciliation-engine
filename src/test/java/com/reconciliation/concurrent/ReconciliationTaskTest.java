package com.reconciliation.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class ReconciliationTaskTest {

    private final LocalDateTime transactionDateTime =
            LocalDateTime.of(2026, 9, 12, 10, 30, 15);

    @Test
    void shouldMatchTransaction() throws Exception {

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

        Map<String, Transaction> externalMap =
                new HashMap<>();

        externalMap.put(
                external.getTransactionId(),
                external);

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        externalMap,
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.MATCHED,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectMissingExternalTransaction() throws Exception {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        Collections.emptyMap(),
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.MISSING_EXTERNAL,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectAmountMismatch() throws Exception {

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

        Map<String, Transaction> externalMap =
                createExternalMap(external);

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        externalMap,
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.AMOUNT_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectStatusMismatch() throws Exception {

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

        Map<String, Transaction> externalMap =
                createExternalMap(external);

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        externalMap,
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.STATUS_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldDetectDateTimeMismatch() throws Exception {

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

        Map<String, Transaction> externalMap =
                createExternalMap(external);

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        externalMap,
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(1, results.size());

        assertEquals(
                ReconciliationStatus.DATE_TIME_MISMATCH,
                results.get(0).getStatus());
    }

    @Test
    void shouldSkipDuplicateTransaction() throws Exception {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Set<String> duplicateInternalIds =
                new HashSet<>();

        duplicateInternalIds.add("TXN1001");

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        Collections.emptyMap(),
                        duplicateInternalIds,
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(
                0,
                results.size());
    }

    @Test
    void shouldSkipDuplicateExternalTransaction() throws Exception {

        Transaction internal =
                createTransaction(
                        "TXN1001",
                        "1000.00",
                        transactionDateTime,
                        TransactionStatus.SUCCESS);

        Set<String> duplicateExternalIds =
                new HashSet<>();

        duplicateExternalIds.add("TXN1001");

        ReconciliationTask task =
                new ReconciliationTask(
                        Arrays.asList(internal),
                        Collections.emptyMap(),
                        Collections.emptySet(),
                        duplicateExternalIds);

        List<ReconciliationResult> results =
                task.call();

        assertEquals(
                0,
                results.size());
    }

    @Test
    void shouldReturnEmptyResultForEmptyTransactionList()
            throws Exception {

        ReconciliationTask task =
                new ReconciliationTask(
                        Collections.emptyList(),
                        Collections.emptyMap(),
                        Collections.emptySet(),
                        Collections.emptySet());

        List<ReconciliationResult> results =
                task.call();

        assertEquals(
                0,
                results.size());
    }

    private Map<String, Transaction> createExternalMap(
            Transaction transaction) {

        Map<String, Transaction> externalMap =
                new HashMap<>();

        externalMap.put(
                transaction.getTransactionId(),
                transaction);

        return externalMap;
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