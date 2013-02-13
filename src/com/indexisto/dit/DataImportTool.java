package com.indexisto.dit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.RequestInfo;
import org.apache.solr.handler.dataimport.writer.SolrWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.data.logger.ImportProcessLogger;
import com.indexisto.dit.data.logger.LogRecord;
import com.indexisto.dit.helper.Util;
import com.indexisto.dit.writer.ElasticWriter;


public class DataImportTool {

    public static final String PROCESS_LOGGER = "processLogger";
    public static final String SNATCHER_URL = "snatcherUrl";

	private static Logger log = LoggerFactory.getLogger(DataImportTool.class);

	//-Djava.util.logging.config.file=logging.properties
	public static void main(String[] args) {
		final ImportProcessLogger processLogger = new ImportProcessLogger();

	    final SolrWriter writer = getTestWriter(processLogger);
		final String mapping = Util.readFileToString("resources/presets/bitrix-iblock_element(recursion-test).xml");
		final Map<String, Object> params = getTestParams();
		params.put(PROCESS_LOGGER, processLogger);

		log.info("Starting DIT test session");
		try {
			snatch(mapping, writer, params);
		} catch (final DataSourceException e) {
			log.error("Data source problem (check snatcher settings)!");
		} catch (final WriterException e) {
			log.error("Writer problem (check writer settings)!");
        } catch (final MappingException e) {
            log.error("Mapping problem (check your mapping)!");
		} catch (final DataImportHandlerException e) {
			log.error("DataImportHandlerException", e);
		} catch (final Exception e) {
			log.error("UNKNOWN PROBLEM", e);
		}

		final List<LogRecord> sqlRequests = processLogger.getSqlRequests();
		for(final LogRecord sqlRequest : sqlRequests) {
		    log.info("request: " + sqlRequest.getMessage());
		}

        final List<LogRecord> sqlResponses = processLogger.getSqlResponses();
        for(final LogRecord sqlResponse : sqlResponses) {
            log.info("response: " + sqlResponse.getMessage());
        }

        final List<LogRecord> documents = processLogger.getDocuments();
        for(final LogRecord document : documents) {
            log.info("document: " + document.getMessage());
        }
	}

	public static void snatch(String mapping, SolrWriter writer,
			Map<String, Object> params) throws DataSourceException, WriterException {
		final DataImporter di = new DataImporter();
		di.loadAndInit(mapping);
		final RequestInfo requestInfo = new RequestInfo(params, null);
		di.runCmd(requestInfo, writer);
	}

	private static SolrWriter getTestWriter(ImportProcessLogger processLogger) {
		return new ElasticWriter("localhost", 9200, "test", "document", "UTF-8", processLogger);
	}

	private static Map<String, Object> getTestParams() {
		final Map<String, Object> dataImportParams = new LinkedHashMap<String, Object>();
		dataImportParams.put("command", "full-import");
		dataImportParams.put("clean", "true");
		dataImportParams.put("commit", "true");
		dataImportParams.put("entity", "document");
		dataImportParams.put("datasource", "com.indexisto.dit.datasource.SnatcherDataSource");
		final Map<String, Object> dataSourceParams = new HashMap<String, Object>();
		dataSourceParams.put("snatcherUrl", "http://46.4.39.138:8082/snatcher.php");
		dataImportParams.put("dataSourceParams", dataSourceParams);
		return dataImportParams;
	}
}

/*+ "<dataSource type=\"JdbcDataSource\" "
+ "driver=\"com.mysql.jdbc.Driver\" "
+ "url=\"jdbc:mysql://localhost/bitrix\" "
+ "user=\"root\" "
+ "password=\"s2s\"/> "*/