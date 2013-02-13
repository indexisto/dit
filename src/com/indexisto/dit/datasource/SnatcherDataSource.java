package com.indexisto.dit.datasource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.DataImportTool;
import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.logger.ImportProcessLogger;
import com.indexisto.dit.helper.HttpClient;
import com.indexisto.dit.helper.Util;

/**
 * <p> A DataSource implementation which can fetch data using client side SQL data snatcher. </p>
 */
public class SnatcherDataSource extends DataSource<Iterator<Map<String, Object>>> {

	private static final Logger LOG = LoggerFactory.getLogger(SnatcherDataSource.class);
    private ImportProcessLogger processLogger;
	protected String snatcherUrl;

	@Override
	public void init(Context context, Properties initProps) {
        processLogger = (ImportProcessLogger) context.getRequestParameters()
                .get(DataImportTool.PROCESS_LOGGER);
        if (processLogger == null) processLogger = new ImportProcessLogger();

		snatcherUrl = initProps.getProperty(DataImportTool.SNATCHER_URL);
		LOG.debug("SnatcherDataSource init with url: " + snatcherUrl);
	}

	@Override
	public Iterator<Map<String, Object>> getData(String query) {
	    query = query.trim();
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		if (snatcherUrl == null || snatcherUrl.length() == 0) return data.iterator();
		String snatch = "";
        processLogger.logSqlRequest(query);

		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new DataSourceException("Can not urlencode query string", e);
		}

		try {
			snatch = HttpClient.httpGet(snatcherUrl + "?query=" + query);
		} catch (final Exception e) {
			throw new DataSourceException("HTTP problem", e);
		}
		processLogger.logSqlResponse(snatch);

		try {
			data = Util.mapJsonArray(snatch);
		} catch (final Exception e) {
			throw new DataSourceException("JSON parsing problem", e);
		}

		return data.iterator();
	}

	@Override
	public void close() {

	}
}
