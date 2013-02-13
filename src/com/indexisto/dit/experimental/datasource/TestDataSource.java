package com.indexisto.dit.experimental.datasource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.datasource.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.HierarchyException;
import com.indexisto.dit.helper.HttpClient;
import com.indexisto.dit.helper.Util;


/**
 * <p> A DataSource implementation which can fetch data using client side SQL data snatcher. </p>
 */
public class TestDataSource extends DataSource<Map<String, Object>> {

	private static final Logger LOG = LoggerFactory.getLogger(TestDataSource.class);

	protected Callable<Connection> factory;

	protected String snatcherUrl;

	@Override
	public void init(Context context, Properties initProps) {
		snatcherUrl = initProps.getProperty("snatcherUrl");
		LOG.info("SnatcherDataSource init with url: " + snatcherUrl);
	}

	@Override
	public Map<String, Object> getData(String query) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		if (snatcherUrl == null || snatcherUrl.length() == 0) return new HashMap<String, Object>();
		String snatch = "";

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

		try {
			data = Util.mapJsonArray(snatch);
		} catch (final Exception e) {
			throw new DataSourceException("JSON parsing problem, or ", e);
		}
		if (data.size() > 1) {
			throw new HierarchyException("More than 1 row in parent record");
		}
		if (data.size() == 0) {
			throw new HierarchyException("No parent row found");
		}

		return data.get(0);
	}

	@Override
	public void close() {

	}
}
