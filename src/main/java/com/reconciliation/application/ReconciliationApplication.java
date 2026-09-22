package com.reconciliation.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.reconciliation.report.ReconciliationReportWriter;
import com.reconciliation.config.ApplicationConfig;
import com.reconciliation.engine.ReconciliationEngine;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.model.BatchProcessingResult;
import com.reconciliation.model.ReconciliationReport;
import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationRun;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;
import com.reconciliation.parser.TransactionFileReader;
import com.reconciliation.parser.TransactionReader;
import com.reconciliation.service.ReconciliationService;

public class ReconciliationApplication {
	
	
	private static final DateTimeFormatter RUN_ID_FORMATTER =
	        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	public static void main(String[] args) {

		try {

			run();

		} catch (ReconciliationException e) {

			System.err.println("Reconciliation processing failed: " + e.getMessage());
		}
	}

	private static void run() throws ReconciliationException {

		ApplicationConfig config = new ApplicationConfig();
		
//		DateTimeFormatter formatter =
//		        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

		LocalDateTime executionTime =
		        LocalDateTime.now();

		String runId =
		        "REC-" + executionTime.format(RUN_ID_FORMATTER);

		ReconciliationRun reconciliationRun =
		        new ReconciliationRun(
		                runId,
		                executionTime);

		TransactionReader transactionReader = new TransactionFileReader();

		BatchProcessingResult internalBatch = transactionReader.readTransactions(config.getInternalTransactionFile());

		BatchProcessingResult externalBatch = transactionReader.readTransactions(config.getExternalTransactionFile());

		List<Transaction> internalTransactions = internalBatch.getValidTransactions();

		List<Transaction> externalTransactions = externalBatch.getValidTransactions();

		ReconciliationEngine reconciliationEngine = new ReconciliationService();

		List<ReconciliationResult> results = reconciliationEngine.reconcile(internalTransactions, externalTransactions);

		ReconciliationReport report = new ReconciliationReport(internalTransactions.size(), externalTransactions.size(),
				results);

		ReconciliationReportWriter reportWriter = new ReconciliationReportWriter();

		reportWriter.writeReport(
		        config.getReconciliationReportFile(),
		        internalBatch,
		        externalBatch,
		        report,
		        reconciliationRun);

		printBatchSummary(internalBatch, externalBatch);

		printValidationIssues(internalBatch, externalBatch);

		printReport(report);

	}

	private static void printBatchSummary(BatchProcessingResult internalBatch, BatchProcessingResult externalBatch) {

		System.out.println();
		System.out.println("========== INPUT BATCH SUMMARY ==========");

		System.out.println("Internal Records Received : " + internalBatch.getTotalRecords());

		System.out.println("Internal Valid Records    : " + internalBatch.getValidRecordCount());

		System.out.println("Internal Invalid Records  : " + internalBatch.getInvalidRecordCount());

		System.out.println();

		System.out.println("External Records Received : " + externalBatch.getTotalRecords());

		System.out.println("External Valid Records    : " + externalBatch.getValidRecordCount());

		System.out.println("External Invalid Records  : " + externalBatch.getInvalidRecordCount());

		System.out.println("==========================================");

	}

	private static void printValidationIssues(BatchProcessingResult internalBatch,
			BatchProcessingResult externalBatch) {

		System.out.println();

		System.out.println("========== INPUT VALIDATION ISSUES ==========");

		if (internalBatch.getValidationErrors().isEmpty()) {

			System.out.println("Internal : None");

		} else {

			System.out.println("Internal:");

			internalBatch.getValidationErrors().forEach(error -> System.out.println("- " + error));
		}

		if (externalBatch.getValidationErrors().isEmpty()) {

			System.out.println("External : None");

		} else {

			System.out.println("External:");

			externalBatch.getValidationErrors().forEach(error -> System.out.println("- " + error));
		}

		System.out.println("=============================================");
	}

	private static void printReport(ReconciliationReport report) {

		System.out.println();
		System.out.println("========== RECONCILIATION SUMMARY ==========");

		System.out.println("Matched               : " + report.getCount(ReconciliationStatus.MATCHED));

		System.out.println("Amount Mismatch       : " + report.getCount(ReconciliationStatus.AMOUNT_MISMATCH));

		System.out.println("Status Mismatch       : " + report.getCount(ReconciliationStatus.STATUS_MISMATCH));

		System.out.println("Date/Time Mismatch    : " + report.getCount(ReconciliationStatus.DATE_TIME_MISMATCH));

		System.out.println("Missing Internal      : " + report.getCount(ReconciliationStatus.MISSING_INTERNAL));

		System.out.println("Missing External      : " + report.getCount(ReconciliationStatus.MISSING_EXTERNAL));

		System.out.println("Duplicates            : " + report.getCount(ReconciliationStatus.DUPLICATE_TRANSACTION));

		System.out.println("============================================");

		System.out.println();
		System.out.println("========== TRANSACTION DETAILS ============");

		for (ReconciliationResult result : report.getResults()) {

			System.out.println(result.getTransactionId() + " => " + result.getStatus() + " | " + result.getReason());
		}

		System.out.println("============================================");
	}

}