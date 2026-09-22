package com.reconciliation.engine;

import java.util.List;

import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.Transaction;

public interface ReconciliationEngine {

    List<ReconciliationResult> reconcile(
            List<Transaction> internalTransactions,
            List<Transaction> externalTransactions);
}