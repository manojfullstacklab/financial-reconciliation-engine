package com.reconciliation.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import com.reconciliation.exception.FileProcessingException;
import com.reconciliation.model.BatchProcessingResult;
import com.reconciliation.model.ReconciliationReport;
import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationRun;
import com.reconciliation.model.ReconciliationStatus;

public class ReconciliationReportWriter {
	
	private static final DateTimeFormatter REPORT_DATE_TIME_FORMATTER =
	        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public void writeReport(String filePath, BatchProcessingResult internalBatch, BatchProcessingResult externalBatch,
			ReconciliationReport report, ReconciliationRun reconciliationRun) {
		
		

		File file = new File(filePath);

		File parentDirectory = file.getParentFile();

		if (parentDirectory != null && !parentDirectory.exists()) {

		    if (!parentDirectory.mkdirs() && !parentDirectory.exists()) {

		        throw new FileProcessingException(
		                "Unable to create report directory: "
		                        + parentDirectory.getAbsolutePath(),
		                null);
		    }
		}

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
		        new FileOutputStream(file),
		        StandardCharsets.UTF_8))) {

			writeLine(writer, "FINANCIAL TRANSACTION RECONCILIATION REPORT");

			writeLine(writer, "============================================");

			writer.newLine();
			
			writeLine(
			        writer,
			        "Run ID          : " + reconciliationRun.getRunId());

			writeLine(
			        writer,
			        "Execution Time  : "
			                + reconciliationRun.getExecutionTime()
			                        .format(REPORT_DATE_TIME_FORMATTER));

			writer.newLine();

			writeLine(writer, "INPUT BATCH SUMMARY");
			writeLine(writer, "-------------------");

			writeLine(writer, "Internal Records Received : " + internalBatch.getTotalRecords());

			writeLine(writer, "Internal Valid Records    : " + internalBatch.getValidRecordCount());

			writeLine(writer, "Internal Invalid Records  : " + internalBatch.getInvalidRecordCount());

			writer.newLine();

			writeLine(writer, "External Records Received : " + externalBatch.getTotalRecords());

			writeLine(writer, "External Valid Records    : " + externalBatch.getValidRecordCount());

			writeLine(writer, "External Invalid Records  : " + externalBatch.getInvalidRecordCount());

			writer.newLine();

			writeLine(writer, "INPUT VALIDATION ISSUES");
			writeLine(writer, "-----------------------");

			writeValidationErrors(writer, "Internal", internalBatch);

			writeValidationErrors(writer, "External", externalBatch);

			writer.newLine();

			writeLine(writer, "RECONCILIATION SUMMARY");
			writeLine(writer, "----------------------");

			writeLine(writer, "Matched               : " + report.getCount(ReconciliationStatus.MATCHED));

			writeLine(writer, "Amount Mismatch       : " + report.getCount(ReconciliationStatus.AMOUNT_MISMATCH));

			writeLine(writer, "Status Mismatch       : " + report.getCount(ReconciliationStatus.STATUS_MISMATCH));

			writeLine(writer, "Date/Time Mismatch    : " + report.getCount(ReconciliationStatus.DATE_TIME_MISMATCH));

			writeLine(writer, "Missing Internal      : " + report.getCount(ReconciliationStatus.MISSING_INTERNAL));

			writeLine(writer, "Missing External      : " + report.getCount(ReconciliationStatus.MISSING_EXTERNAL));

			writeLine(writer, "Duplicates            : " + report.getCount(ReconciliationStatus.DUPLICATE_TRANSACTION));

			writer.newLine();

			writeLine(writer, "TRANSACTION DETAILS");
			writeLine(writer, "-------------------");

			for (ReconciliationResult result : report.getResults()) {

				writeLine(writer, result.getTransactionId() + " => " + result.getStatus() + " | " + result.getReason());
			}

			writer.newLine();

			writeLine(writer, "============================================");

			writeLine(writer, "END OF RECONCILIATION REPORT");

		} catch (IOException e) {

			throw new FileProcessingException("Failed to write reconciliation report: " + filePath, e);
		}
	}

	private void writeValidationErrors(BufferedWriter writer, String source, BatchProcessingResult batch)
			throws IOException {

		if (batch.getValidationErrors().isEmpty()) {

			writeLine(writer, source + " : None");

		} else {

			writeLine(writer, source + ":");

			for (String error : batch.getValidationErrors()) {

				writeLine(writer, "- " + error);
			}
		}
	}

	private void writeLine(BufferedWriter writer, String text) throws IOException {

		writer.write(text);
		writer.newLine();
	}
}