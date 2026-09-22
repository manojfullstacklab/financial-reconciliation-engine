package com.reconciliation.model;

public class ReconciliationResult {

    private final String transactionId;
    private final ReconciliationStatus status;
    private final String reason;

    public ReconciliationResult(
            String transactionId,
            ReconciliationStatus status,
            String reason) {

        this.transactionId = transactionId;
        this.status = status;
        this.reason = reason;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}