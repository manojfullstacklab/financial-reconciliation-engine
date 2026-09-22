package com.reconciliation.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.reconciliation.exception.FileProcessingException;
import com.reconciliation.model.BatchProcessingResult;

public class TransactionFileReaderTest {
	
	@Test
	void shouldReadValidAndInvalidTransactions() {

	    TransactionFileReader reader =
	            new TransactionFileReader();

	    BatchProcessingResult result =
	            reader.readTransactions("input/internal-transactions.csv");

	    assertEquals(5, result.getTotalRecords());

	    assertEquals(4, result.getValidRecordCount());

	    assertEquals(1, result.getInvalidRecordCount());
	}
	
	
	@Test
	void shouldReadExternalTransactions() {

	    TransactionFileReader reader =
	            new TransactionFileReader();

	    BatchProcessingResult result =
	            reader.readTransactions("input/external-transactions.csv");

	    assertEquals(5, result.getTotalRecords());

	    assertEquals(5, result.getValidRecordCount());

	    assertEquals(0, result.getInvalidRecordCount());
	}
	
	
	@Test
	void shouldThrowExceptionWhenFileDoesNotExist() {

	    TransactionFileReader reader =
	            new TransactionFileReader();

	    assertThrows(
	            FileProcessingException.class,
	            () -> reader.readTransactions(
	                    "input/file-does-not-exist.csv"));
	}

}
