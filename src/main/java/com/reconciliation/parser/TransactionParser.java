package com.reconciliation.parser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.reconciliation.exception.InvalidTransactionException;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class TransactionParser {

	public Transaction parse(String line) {

		String[] data = line.split(",");

		if (data.length != 4) {

			throw new InvalidTransactionException(

					"Invalid transaction record: " + line
			);
		}
		
		try {
			
			String transactionId = data[0];
			
			BigDecimal amount = new BigDecimal(data[1]);
			
			LocalDateTime transactionDateTime = LocalDateTime.parse(data[2]);
			
			TransactionStatus status = TransactionStatus.valueOf(data[3].trim().toUpperCase());

			return new Transaction(transactionId, amount, transactionDateTime, status);

		} catch (IllegalArgumentException |
			       DateTimeParseException e) {

		    throw new InvalidTransactionException(
		            "Invalid transaction data: " + line);
		}

	}

}