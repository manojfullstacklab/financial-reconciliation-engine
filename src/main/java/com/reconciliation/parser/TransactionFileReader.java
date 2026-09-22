package com.reconciliation.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reconciliation.exception.FileProcessingException;
import com.reconciliation.exception.InvalidTransactionException;
import com.reconciliation.model.BatchProcessingResult;
import com.reconciliation.model.Transaction;
import com.reconciliation.validator.TransactionValidator;

public class TransactionFileReader implements TransactionReader{
	
	private static final Logger LOGGER =
            Logger.getLogger(TransactionFileReader.class.getName());
	
	private final TransactionParser parser = new TransactionParser();

	private final TransactionValidator validator = new TransactionValidator();

    
    @Override
    public BatchProcessingResult readTransactions(
            String resourcePath) {

        List<Transaction> transactions =
                new ArrayList<>();

        List<String> validationErrors =
                new ArrayList<>();

        int totalRecords = 0;

		/*
		 * TransactionParser parser = new TransactionParser();
		 * 
		 * TransactionValidator validator = new TransactionValidator();
		 */

        try (InputStream inputStream =
                     getClass().getClassLoader()
                             .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {

                throw new FileProcessingException(
                        "Transaction file not found: "
                                + resourcePath,
                        null);
            }

            try (BufferedReader reader =
                         new BufferedReader(
                        		 new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                // Skip CSV header
                reader.readLine();

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    totalRecords++;

                    try {

                        Transaction transaction =
                                parser.parse(line);

                        validator.validate(transaction);

                        transactions.add(transaction);

                    } catch (InvalidTransactionException
                            | IllegalArgumentException e) {

                        String error =
                                "Invalid transaction: " + line;

                        validationErrors.add(error);

                        LOGGER.warning(error);
                    }
                }
            }

        } catch (IOException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Error while reading transaction file: "
                            + resourcePath,
                    e);

            throw new FileProcessingException(
                    "Unable to process transaction file: "
                            + resourcePath,
                    e);
        }

        LOGGER.info(
                "Processed "
                        + totalRecords
                        + " records from "
                        + resourcePath
                        + ". Valid: "
                        + transactions.size()
                        + ", Invalid: "
                        + validationErrors.size());

        return new BatchProcessingResult(
                totalRecords,
                transactions,
                validationErrors);
    }
}