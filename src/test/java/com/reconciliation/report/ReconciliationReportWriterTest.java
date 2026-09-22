package com.reconciliation.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.reconciliation.model.ReconciliationRun;

import org.junit.jupiter.api.Test;

import com.reconciliation.model.BatchProcessingResult;
import com.reconciliation.model.ReconciliationReport;
import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;
import com.reconciliation.model.TransactionStatus;

public class ReconciliationReportWriterTest {

    @Test
    void shouldCreateReconciliationReportFile() throws Exception {

        // --------------------------------------------------
        // 1. Create sample transaction
        // --------------------------------------------------

        Transaction transaction = new Transaction(
                "TXN1001",
                new BigDecimal("1000.00"),
                LocalDateTime.of(
                        2026,
                        9,
                        22,
                        10,
                        30,
                        15),
                TransactionStatus.SUCCESS
        );

        // --------------------------------------------------
        // 2. Create internal batch
        // --------------------------------------------------

        BatchProcessingResult internalBatch =
                new BatchProcessingResult(
                        1,
                        Arrays.asList(transaction),
                        Collections.emptyList()
                );

        // --------------------------------------------------
        // 3. Create external batch
        // --------------------------------------------------

        BatchProcessingResult externalBatch =
                new BatchProcessingResult(
                        1,
                        Arrays.asList(transaction),
                        Collections.emptyList()
                );

        // --------------------------------------------------
        // 4. Create reconciliation result
        // --------------------------------------------------

        ReconciliationResult reconciliationResult =
                new ReconciliationResult(
                        "TXN1001",
                        ReconciliationStatus.MATCHED,
                        "Transaction matched successfully"
                );

        List<ReconciliationResult> results =
                Arrays.asList(reconciliationResult);

        // --------------------------------------------------
        // 5. Create reconciliation report
        // --------------------------------------------------

        ReconciliationReport report =
                new ReconciliationReport(
                        internalBatch.getValidRecordCount(),
                        externalBatch.getValidRecordCount(),
                        results
                );

        // --------------------------------------------------
        // 6. Create temporary output file
        // --------------------------------------------------

        File tempFile = File.createTempFile(
                "reconciliation-report-test",
                ".txt"
        );

        try {

            // --------------------------------------------------
            // 7. Create report writer
            // --------------------------------------------------

            ReconciliationReportWriter reportWriter =
                    new ReconciliationReportWriter();
            
            
         // --------------------------------------------------
         // 8. Create reconciliation run
         // --------------------------------------------------

         ReconciliationRun reconciliationRun =
                 new ReconciliationRun(
                         "REC-TEST-001",
                         LocalDateTime.of(
                                 2026,
                                 9,
                                 22,
                                 10,
                                 30,
                                 0
                         )
                 );

            // --------------------------------------------------
            // 9. Write report
            // --------------------------------------------------

            reportWriter.writeReport(
                    tempFile.getAbsolutePath(),
                    internalBatch,
                    externalBatch,
                    report,
            reconciliationRun);
            

            // --------------------------------------------------
            // 10. Verify file was created
            // --------------------------------------------------

            assertTrue(tempFile.exists());

            // --------------------------------------------------
            // 11. Verify file is not empty
            // --------------------------------------------------

            assertTrue(tempFile.length() > 0);

        } finally {

            // --------------------------------------------------
            // 12. Delete temporary file
            // --------------------------------------------------

            Files.deleteIfExists(tempFile.toPath());
        }
    }
}