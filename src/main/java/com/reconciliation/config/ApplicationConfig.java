package com.reconciliation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.reconciliation.exception.ConfigurationException;

public class ApplicationConfig {

    private final Properties properties;

    public ApplicationConfig() {

        properties = new Properties();

        try (InputStream inputStream =
                     getClass().getClassLoader()
                             .getResourceAsStream("application.properties")) {

            if (inputStream == null) {
                throw new ConfigurationException(
                        "application.properties not found",
                        null);
            }

            properties.load(inputStream);

        } catch (IOException e) {

            throw new ConfigurationException(
                    "Unable to load application configuration",
                    e);
        }
    }

    public String getInternalTransactionFile() {
        return getRequiredProperty(
                "internal.transaction.file");
    }

    public String getExternalTransactionFile() {
        return getRequiredProperty(
                "external.transaction.file");
    }

    private String getRequiredProperty(String key) {

        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {

            throw new ConfigurationException(
                    "Missing configuration property: " + key,
                    null);
        }

        return value.trim();
    }
    
    public String getReconciliationReportFile() {

        return getRequiredProperty(
                "reconciliation.report.file");
    }
}