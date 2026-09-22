package com.reconciliation.model;

public enum ReconciliationStatus {

    MATCHED,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH,
    DATE_TIME_MISMATCH,
    MISSING_INTERNAL,
    MISSING_EXTERNAL,
    DUPLICATE_TRANSACTION
}