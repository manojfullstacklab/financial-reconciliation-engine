package com.reconciliation.parser;

import com.reconciliation.model.BatchProcessingResult;

public interface TransactionReader {

    BatchProcessingResult readTransactions(
            String resourcePath);
}