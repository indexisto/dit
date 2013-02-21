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

import com.indexisto.dit.DataImportHandler;
import com.indexisto.dit.data.cache.DihCache;
import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.logger.DihProcessLogger;
import com.indexisto.dit.helper.HttpClient;
import com.indexisto.dit.helper.Util;

/**
 * <p> A DataSource implementation which can fetch data using client side SQL data snatcher. </p>
 */
public class SnatcherDataSource extends DataSource<Iterator<Map<String, Object>>> {

	private static final Logger LOG = LoggerFactory.getLogger(SnatcherDataSource.class);
    private DihProcessLogger processLogger;
	protected String snatcherUrl;
	protected DihCache cache;

	@Override
	public void init(Context context, Properties initProps) {
        processLogger = (DihProcessLogger) context.getRequestParameters()
                .get(DataImportHandler.PROCESS_LOGGER);
        cache = (DihCache) context.getRequestParameters()
                .get(DataImportHandler.CACHE);

        snatcherUrl = context.getEntityAttribute("agent");
        if (snatcherUrl == null || snatcherUrl.equals("")) {
            throw new MappingException("Agent not specefied");
        }
		LOG.debug("SnatcherDataSource init with url: " + snatcherUrl);
	}

	@Override
	public Iterator<Map<String, Object>> getData(String query) {
	    query = query.trim();

	    List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		String snatch = cache.get(query);
		processLogger.logSqlRequest(query);

        if (snatch == null) {
            String encodedQuery = "";
    		try {
    		    encodedQuery = URLEncoder.encode(query, "UTF-8");
    		} catch (final UnsupportedEncodingException e) {
    			throw new DataSourceException("Can not urlencode query string", e);
    		}

    		try {
    			snatch = HttpClient.httpGet(snatcherUrl + "?query=" + encodedQuery);
    		} catch (final Exception e) {
    			throw new DataSourceException("HTTP problem", e);
    		}
    		cache.put(query, snatch);
    		processLogger.logSqlResponse(snatch);
        } else {
            processLogger.logCacheHit();
        }

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
