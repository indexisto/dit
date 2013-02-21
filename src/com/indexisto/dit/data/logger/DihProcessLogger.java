package com.indexisto.dit.data.logger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This is temporary logger
 * for development and testing purposes
 * TODO: make id based on streams
 */
public class DihProcessLogger {

    private static Logger log = LoggerFactory.getLogger(DihProcessLogger.class);

    private final DihLogLevel level;

    private final List<DihLogRecord> sqlRequests;
    private int sqlRequestsCount = 0;

    private final List<DihLogRecord> sqlResponses;
    private int sqlResponsesCount = 0;

    private final List<DihLogRecord> documents;
    private int documentsCount = 0;

    private int dihCacheHitsCount = 0;

    public DihProcessLogger() {
        this(DihLogLevel.DEBUG);
    }

    public DihProcessLogger(DihLogLevel level) {
        this.level = level;
        sqlRequests = new ArrayList<DihLogRecord>();
        sqlResponses = new ArrayList<DihLogRecord>();
        documents = new ArrayList<DihLogRecord>();
    }

    public void logSqlRequest(String sqlRequest) {
        if (level.getId() < DihLogLevel.INFO.getId()) sqlRequests.add(new DihLogRecord(sqlRequest.replaceAll("\\s+", " ")));
        sqlRequestsCount++;
        sendLogMessage("Sql request made: [" + sqlRequest.replaceAll("\\s+", " ") + "]");
    }

    public void logSqlResponse(String sqlResponse) {
        if (level.getId() < DihLogLevel.INFO.getId()) sqlResponses.add(new DihLogRecord(sqlResponse));
        sqlResponsesCount++;
        sendLogMessage("Sql response received: " + sqlResponse);
    }

    public void logDocument(String document) {
        if (level.getId() < DihLogLevel.INFO.getId()) documents.add(new DihLogRecord(document));
        documentsCount++;
        sendLogMessage("Document created: " + document);
    }

    public void logCacheHit() {
        dihCacheHitsCount++;
    }

    public List<DihLogRecord> getSqlRequests() {
        return sqlRequests;
    }

    public int getSqlRequestsCount() {
        return sqlRequestsCount;
    }

    public List<DihLogRecord> getSqlResponses() {
        return sqlResponses;
    }

    public int getSqlResponsesCount() {
        return sqlResponsesCount;
    }

    public List<DihLogRecord> getDocuments() {
        return documents;
    }

    public int getDocumentsCount() {
        return documentsCount;
    }

    public int getCacheHitsCount() {
        return dihCacheHitsCount;
    }

    private void sendLogMessage(String message) {
        sendLogMessage(message, level);
    }

    private void sendLogMessage(String message, DihLogLevel level) {
        if (level == DihLogLevel.DEBUG) {
            log.debug(message);
        }
        if (level == DihLogLevel.INFO) {
            log.info(message);
        }
        if (level == DihLogLevel.VERBOSE) {
            log.trace(message);
        }
    }

    @Override
    public String toString() {
        return "DihProcessLogger [level: " + level + ", sql requests: "
                + sqlRequestsCount + ", sql responses: " + sqlResponsesCount
                + ", cache hits: " + dihCacheHitsCount
                + ", documents: " + documentsCount + "]";
    }
}