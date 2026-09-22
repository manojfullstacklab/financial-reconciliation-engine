package com.reconciliation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.reconciliation.concurrent.ReconciliationTask;
import com.reconciliation.engine.ReconciliationEngine;
import com.reconciliation.exception.ReconciliationException;
import com.reconciliation.model.ReconciliationResult;
import com.reconciliation.model.ReconciliationStatus;
import com.reconciliation.model.Transaction;

public class ReconciliationService implements ReconciliationEngine {

	private static final Logger LOGGER = Logger.getLogger(ReconciliationService.class.getName());

	private static final int PARALLEL_PROCESSING_THRESHOLD = 1000;

	private static final int BATCH_SIZE = 500;

	@Override
	public List<ReconciliationResult> reconcile(List<Transaction> internalTransactions,
			List<Transaction> externalTransactions) {

		LOGGER.info("Starting reconciliation...");

		Map<String, Transaction> internalTransactionMap = new HashMap<>();

		Map<String, Transaction> externalTransactionMap = new HashMap<>();

		Set<String> duplicateInternalIds = findDuplicates(internalTransactions, internalTransactionMap);

		Set<String> duplicateExternalIds = findDuplicates(externalTransactions, externalTransactionMap);

		List<ReconciliationResult> results = reconcileInternalTransactions(internalTransactions, externalTransactionMap,
				duplicateInternalIds, duplicateExternalIds);

		addMissingInternalTransactions(externalTransactions, internalTransactionMap, duplicateInternalIds,
				duplicateExternalIds, results);

		return results;
	}

	
	
	
	private List<ReconciliationResult> reconcileInternalTransactions(List<Transaction> internalTransactions,
			Map<String, Transaction> externalTransactionMap, Set<String> duplicateInternalIds,
			Set<String> duplicateExternalIds) {

		if (internalTransactions.size() < PARALLEL_PROCESSING_THRESHOLD) {

			return reconcileSequentially(internalTransactions, externalTransactionMap, duplicateInternalIds,
					duplicateExternalIds);
		}

		return reconcileInParallel(internalTransactions, externalTransactionMap, duplicateInternalIds,
				duplicateExternalIds);
	}

	private List<ReconciliationResult> reconcileSequentially(List<Transaction> transactions,
			Map<String, Transaction> externalTransactionMap, Set<String> duplicateInternalIds,
			Set<String> duplicateExternalIds) {

		List<ReconciliationResult> results = new ArrayList<>();

		Set<String> reportedDuplicateIds = new HashSet<>();

		for (Transaction internalTransaction : transactions) {

			String transactionId = internalTransaction.getTransactionId();

			if (duplicateInternalIds.contains(transactionId) || duplicateExternalIds.contains(transactionId)) {

				if (reportedDuplicateIds.add(transactionId)) {

					results.add(new ReconciliationResult(transactionId, ReconciliationStatus.DUPLICATE_TRANSACTION,
							"Duplicate transaction ID detected"));
				}

				continue;
			}

			Transaction externalTransaction = externalTransactionMap.get(transactionId);

			if (externalTransaction == null) {

				results.add(new ReconciliationResult(transactionId, ReconciliationStatus.MISSING_EXTERNAL,
						"Transaction not found in valid external system"));

				continue;
			}

			if (internalTransaction.getAmount().compareTo(externalTransaction.getAmount()) != 0) {

				results.add(new ReconciliationResult(transactionId, ReconciliationStatus.AMOUNT_MISMATCH,
						"Internal amount = " + internalTransaction.getAmount() + ", External amount = "
								+ externalTransaction.getAmount()));

				continue;
			}

			if (!internalTransaction.getStatus().equals(externalTransaction.getStatus())) {

				results.add(new ReconciliationResult(transactionId, ReconciliationStatus.STATUS_MISMATCH,
						"Internal status = " + internalTransaction.getStatus() + ", External status = "
								+ externalTransaction.getStatus()));

				continue;
			}

			if (!internalTransaction.getTransactionDateTime().equals(externalTransaction.getTransactionDateTime())) {

				results.add(new ReconciliationResult(transactionId, ReconciliationStatus.DATE_TIME_MISMATCH,
						"Internal date/time = " + internalTransaction.getTransactionDateTime()
								+ ", External date/time = " + externalTransaction.getTransactionDateTime()));

				continue;
			}

			results.add(new ReconciliationResult(transactionId, ReconciliationStatus.MATCHED,
					"Transaction matched successfully"));
		}

		return results;
	}

	private List<ReconciliationResult> reconcileInParallel(List<Transaction> transactions,
			Map<String, Transaction> externalTransactionMap, Set<String> duplicateInternalIds,
			Set<String> duplicateExternalIds) {

		int availableProcessors = Runtime.getRuntime().availableProcessors();

		int threadCount = Math.min(availableProcessors, (transactions.size() + BATCH_SIZE - 1) / BATCH_SIZE);

		LOGGER.info("Large batch detected. Using " + threadCount + " worker threads.");

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		List<Future<List<ReconciliationResult>>> futures = new ArrayList<>();

		try {

			for (int start = 0; start < transactions.size(); start += BATCH_SIZE) {

				int end = Math.min(start + BATCH_SIZE, transactions.size());

				List<Transaction> batch = transactions.subList(start, end);

				ReconciliationTask task = new ReconciliationTask(batch, externalTransactionMap, duplicateInternalIds,
						duplicateExternalIds);

				futures.add(executor.submit(task));
			}

			List<ReconciliationResult> results = new ArrayList<>();

			for (Future<List<ReconciliationResult>> future : futures) {

				results.addAll(future.get());
			}

			addDuplicateResults(duplicateInternalIds, duplicateExternalIds, results);

			return results;

		} catch (InterruptedException e) {

		    Thread.currentThread().interrupt();

		    throw new ReconciliationException(
		            "Reconciliation processing interrupted",
		            e);
		}

		 catch (ExecutionException e) {

			throw new ReconciliationException("Error during parallel reconciliation", e);
			
		} finally {

			executor.shutdown();
		}
	}

	private void addDuplicateResults(
	        Set<String> duplicateInternalIds,
	        Set<String> duplicateExternalIds,
	        List<ReconciliationResult> results) {

	    Set<String> existingIds = new HashSet<>();

	    for (ReconciliationResult result : results) {
	        existingIds.add(result.getTransactionId());
	    }

	    Set<String> duplicateIds = new HashSet<>();

	    duplicateIds.addAll(duplicateInternalIds);
	    duplicateIds.addAll(duplicateExternalIds);

	    for (String duplicateId : duplicateIds) {

	        if (!existingIds.contains(duplicateId)) {

	            results.add(
	                    new ReconciliationResult(
	                            duplicateId,
	                            ReconciliationStatus.DUPLICATE_TRANSACTION,
	                            "Duplicate transaction ID detected"));
	        }
	    }
	}

	private void addMissingInternalTransactions(List<Transaction> externalTransactions,
			Map<String, Transaction> internalTransactionMap, Set<String> duplicateInternalIds,
			Set<String> duplicateExternalIds, List<ReconciliationResult> results) {

		for (Transaction externalTransaction : externalTransactions) {

			String transactionId = externalTransaction.getTransactionId();

			if (duplicateExternalIds.contains(transactionId) || duplicateInternalIds.contains(transactionId)) {
				continue;
			}

			if (!internalTransactionMap.containsKey(transactionId)) {

				results.add(new ReconciliationResult(transactionId, ReconciliationStatus.MISSING_INTERNAL,
						"Transaction not found in valid internal records"));
			}
		}
	}

	private Set<String> findDuplicates(List<Transaction> transactions, Map<String, Transaction> transactionMap) {

		Set<String> seenIds = new HashSet<>();

		Set<String> duplicateIds = new HashSet<>();

		for (Transaction transaction : transactions) {

			String transactionId = transaction.getTransactionId();

			if (!seenIds.add(transactionId)) {
				duplicateIds.add(transactionId);
			}

			transactionMap.put(transactionId, transaction);
		}

		return duplicateIds;
	}
}