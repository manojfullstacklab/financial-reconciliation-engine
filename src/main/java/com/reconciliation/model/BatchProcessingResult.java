package com.reconciliation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatchProcessingResult {

    private final int totalRecords;
    private final List<Transaction> validTransactions;
    private final List<String> validationErrors;

    public BatchProcessingResult(
            int totalRecords,
            List<Transaction> validTransactions,
            List<String> validationErrors) {

        this.totalRecords = totalRecords;

        this.validTransactions =
                Collections.unmodifiableList(
                        new ArrayList<>(validTransactions));

        this.validationErrors =
                Collections.unmodifiableList(
                        new ArrayList<>(validationErrors));
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public List<Transaction> getValidTransactions() {
        return validTransactions;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public int getValidRecordCount() {
        return validTransactions.size();
    }

    public int getInvalidRecordCount() {
        return validationErrors.size();
    }
}