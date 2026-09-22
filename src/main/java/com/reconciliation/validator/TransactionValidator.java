package com.reconciliation.validator;

import com.reconciliation.exception.InvalidTransactionException;

import com.reconciliation.model.Transaction;

public class TransactionValidator {

    public void validate(Transaction transaction) {

        if (transaction == null) {
        	
        	throw new InvalidTransactionException("Transaction cannot be null");
        }

        if (transaction.getTransactionId() == null
                || transaction.getTransactionId().trim().isEmpty()) {

        	throw new InvalidTransactionException("Transaction ID cannot be empty");
        }

        if (transaction.getAmount() == null
                || transaction.getAmount().signum() <= 0) {

        	throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }

        if (transaction.getTransactionDateTime() == null) {

        	throw new InvalidTransactionException("Transaction date/time cannot be null");
        }

        if (transaction.getStatus() == null) {

        	throw new InvalidTransactionException("Transaction status cannot be empty");
        }
    }
}