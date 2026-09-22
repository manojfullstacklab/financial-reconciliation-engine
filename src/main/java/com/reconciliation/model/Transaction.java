package com.reconciliation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Transaction {

	private final String transactionId;
	private final BigDecimal amount;
	private final LocalDateTime transactionDateTime;
	private final TransactionStatus status;

	public Transaction(String transactionId, BigDecimal amount, LocalDateTime transactionDateTime,
			TransactionStatus status) {

		this.transactionId = transactionId;
		this.amount = amount;
		this.transactionDateTime = transactionDateTime;
		this.status = status;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public LocalDateTime getTransactionDateTime() {
		return transactionDateTime;
	}

	public TransactionStatus getStatus() {
		return status;
	}

}
