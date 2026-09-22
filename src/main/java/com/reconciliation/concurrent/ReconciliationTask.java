package com.reconciliation.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;

public class ReconciliationTask
        implements Callable<List<ReconciliationResult>> {

    private final List<Transaction> transactions;
    private final Map<String, Transaction> externalTransactionMap;
    private final Set<String> duplicateInternalIds;
    private final Set<String> duplicateExternalIds;

    public ReconciliationTask(
            List<Transaction> transactions,
            Map<String, Transaction> externalTransactionMap,
            Set<String> duplicateInternalIds,
            Set<String> duplicateExternalIds) {

        this.transactions = transactions;
        this.externalTransactionMap = externalTransactionMap;
        this.duplicateInternalIds = duplicateInternalIds;
        this.duplicateExternalIds = duplicateExternalIds;
    }

    @Override
    public List<ReconciliationResult> call() {

        List<ReconciliationResult> results =
                new ArrayList<>();

        for (Transaction internalTransaction : transactions) {

            String transactionId =
                    internalTransaction.getTransactionId();

            if (duplicateInternalIds.contains(transactionId)
                    || duplicateExternalIds.contains(transactionId)) {

                continue;
            }

            Transaction externalTransaction =
                    externalTransactionMap.get(transactionId);

            if (externalTransaction == null) {

                results.add(new ReconciliationResult(
                        transactionId,
                        ReconciliationStatus.MISSING_EXTERNAL,
                        "Transaction not found in external system"));

                continue;
            }

            if (internalTransaction.getAmount()
                    .compareTo(externalTransaction.getAmount()) != 0) {

                results.add(new ReconciliationResult(
                        transactionId,
                        ReconciliationStatus.AMOUNT_MISMATCH,
                        "Internal amount = "
                                + internalTransaction.getAmount()
                                + ", External amount = "
                                + externalTransaction.getAmount()));

                continue;
            }

            if (!internalTransaction.getStatus()
                    .equals(externalTransaction.getStatus())) {

                results.add(new ReconciliationResult(
                        transactionId,
                        ReconciliationStatus.STATUS_MISMATCH,
                        "Internal status = "
                                + internalTransaction.getStatus()
                                + ", External status = "
                                + externalTransaction.getStatus()));

                continue;
            }

            if (!internalTransaction.getTransactionDateTime()
                    .equals(externalTransaction.getTransactionDateTime())) {

                results.add(new ReconciliationResult(
                        transactionId,
                        ReconciliationStatus.DATE_TIME_MISMATCH,
                        "Internal date/time = "
                                + internalTransaction.getTransactionDateTime()
                                + ", External date/time = "
                                + externalTransaction.getTransactionDateTime()));

                continue;
            }

            results.add(new ReconciliationResult(
                    transactionId,
                    ReconciliationStatus.MATCHED,
                    "Transaction matched successfully"));
        }

        return results;
    }
}