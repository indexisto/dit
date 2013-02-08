package com.indexisto.dit.data.logger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is temporary logger
 * for development and testin purposes
 * TODO: make id based on streams
 */
public class ImportProcessLogger {

    private static Logger log = LoggerFactory.getLogger(ImportProcessLogger.class);

    private final List<LogRecord> sqlRequests;
    private final List<LogRecord> sqlResponses;
    private final List<LogRecord> documents;

    public ImportProcessLogger() {
        sqlRequests = new ArrayList<LogRecord>();
        sqlResponses = new ArrayList<LogRecord>();
        documents = new ArrayList<LogRecord>();
    }

    public void logSqlRequest(String sqlRequest) {
        sqlRequests.add(new LogRecord(sqlRequest.replaceAll("\\s+", " ")));
    }

    public void logSqlResponse(String sqlResponse) {
        sqlResponses.add(new LogRecord(sqlResponse));
    }

    public void logDocument(String document) {
        documents.add(new LogRecord(document));
    }


    public List<LogRecord> getSqlRequests() {
        return sqlRequests;
    }

    public List<LogRecord> getSqlResponses() {
        return sqlResponses;
    }

    public List<LogRecord> getDocuments() {
        return documents;
    }
}