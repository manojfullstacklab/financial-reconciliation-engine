package com.reconciliation.model;

import java.time.LocalDateTime;

public class ReconciliationRun {

    private final String runId;

    private final LocalDateTime executionTime;

    public ReconciliationRun(
            String runId,
            LocalDateTime executionTime) {

        this.runId = runId;
        this.executionTime = executionTime;
    }

    public String getRunId() {
        return runId;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
}