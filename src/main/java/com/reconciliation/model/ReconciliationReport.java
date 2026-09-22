package com.reconciliation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReconciliationReport {

    private final int internalTransactionCount;
    private final int externalTransactionCount;
    private final List<ReconciliationResult> results;
    private final Map<ReconciliationStatus, Long> statusCounts;

    public ReconciliationReport(
            int internalTransactionCount,
            int externalTransactionCount,
            List<ReconciliationResult> results) {

        this.internalTransactionCount = internalTransactionCount;
        this.externalTransactionCount = externalTransactionCount;
        this.results = Collections.unmodifiableList(
                new ArrayList<>(
                        results.stream()
                                .sorted(
                                        Comparator.comparing(
                                                ReconciliationResult::getTransactionId))
                                .collect(Collectors.toList())
                )
        );

        this.statusCounts = results.stream()
                .collect(Collectors.groupingBy(
                        ReconciliationResult::getStatus,
                        Collectors.counting()));
    }

    public int getInternalTransactionCount() {
        return internalTransactionCount;
    }

    public int getExternalTransactionCount() {
        return externalTransactionCount;
    }

    public List<ReconciliationResult> getResults() {
        return results;
    }

    public long getCount(ReconciliationStatus status) {
        return statusCounts.getOrDefault(status, 0L);
    }
}