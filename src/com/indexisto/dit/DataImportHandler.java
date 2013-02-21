package com.indexisto.dit;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.cache.DihCache;
import com.indexisto.dit.data.logger.DihProcessLogger;
import com.indexisto.dit.data.object.DihCommandEnum;
import com.indexisto.dit.processor.LimitedSqlEntityProcessor;
import com.indexisto.dit.writer.IndexistoWriter;


public class DataImportHandler {
    public static final int DEFAULT_CACHE_SIZE = 1000;

    public static final String PROCESS_LOGGER = "processLogger";
    public static final String COMMAND = "command";
    public static final String CACHE = "cache";
    public static final String DEFAULT_DATASOURCE = "datasource";
    public static final String DEFAULT_PROCESSOR = "processor";

	private static Logger log = LoggerFactory.getLogger(DataImportHandler.class);
	private final DihProcessLogger processLogger;

	private final Map<String, Object> dataImportParams;
	private final DataImporter dataImporter;
	private final IndexistoWriter writer;
	private final DihCache cache;

	public DataImportHandler(String mapping, String entity,
	        IndexistoWriter writer, DihCache cache, DihProcessLogger logger) {

	    if (logger == null) logger = new DihProcessLogger();
	    processLogger = logger;

	    writer.setProcessLogger(processLogger);
	    this.writer = writer;

	    if (cache == null) cache = new DihCache(DEFAULT_CACHE_SIZE);
	    this.cache = cache;

        dataImportParams = new LinkedHashMap<String, Object>();
        dataImportParams.put("entity", entity);
        dataImportParams.put("clean", "true");
        dataImportParams.put("commit", "true");
        dataImportParams.put(CACHE, cache);
        dataImportParams.put(DEFAULT_DATASOURCE, "com.indexisto.dit.datasource.SnatcherDataSource");
        dataImportParams.put(DEFAULT_PROCESSOR, "com.indexisto.dit.processor.LimitedSqlEntityProcessor");
        dataImportParams.put(DataImportHandler.PROCESS_LOGGER, processLogger);

		dataImporter = new DataImporter();
		dataImporter.loadAndInit(mapping);
	}

	public DihProcessLogger doFullImport(long offset, long limit) {
	    return doFullImport(offset, limit, false);
	}

	public DihProcessLogger doFullImport(long offset, long limit, boolean checkRun) {
        dataImportParams.put(COMMAND, DihCommandEnum.FULL_IMPORT.toString());
        dataImportParams.put(LimitedSqlEntityProcessor.OFFSET, offset);
        dataImportParams.put(LimitedSqlEntityProcessor.LIMIT, limit);
        final RequestInfo requestInfo = new RequestInfo(dataImportParams, null);
        dataImporter.runCmd(requestInfo, writer);

	    return processLogger;
	}

	public DihProcessLogger doDeltaImport(long offset, long limit, Date lastIndexTime) {
	    return doDeltaImport(offset, limit, lastIndexTime, false);
	}

    public DihProcessLogger doDeltaImport(long offset, long limit, Date lastIndexTime, boolean checkRun) {
        dataImportParams.put(COMMAND, DihCommandEnum.DELTA_IMPORT.toString());
        dataImportParams.put(LimitedSqlEntityProcessor.OFFSET, offset);
        dataImportParams.put(LimitedSqlEntityProcessor.LIMIT, limit);
        dataImportParams.put(LimitedSqlEntityProcessor.LAST_INDEX_TIME, lastIndexTime);
        final RequestInfo requestInfo = new RequestInfo(dataImportParams, null);
        dataImporter.runCmd(requestInfo, writer);

        return processLogger;
    }

    public DihCache getCache() {
        return cache;
    }
}