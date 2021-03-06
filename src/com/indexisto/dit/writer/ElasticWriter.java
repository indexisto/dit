package com.indexisto.dit.writer;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.helper.HttpClient;
import com.indexisto.dit.helper.SolrDocumentConverter;

public class ElasticWriter extends IndexistoWriter {

	private final String baseUrl;
	private String charSet = "UTF-8"; //"Cp1251";
	Boolean deleteAllCalled;
	Boolean commitCalled;
	List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
	private static Logger log = LoggerFactory.getLogger(ElasticWriter.class);

	public ElasticWriter(String domain, int port, String index,
	        String entity, String charset) {
		super();

		final StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append("http://").
			append(domain).
			append(":").append(port).
			append("/").append(index).
			append("/").append(entity).
			append("/");
		baseUrl = urlBuilder.toString();
		charSet = charset;
	}

    @Override
	public boolean upload(SolrInputDocument doc) {
		log.debug("upload()");
		return docs.add(doc);
	}

	@Override
	public void doDeleteAll() {
		log.debug("doDeleteAll()");
		deleteAllCalled = Boolean.TRUE;
	}

	@Override
	public void commit(boolean b) {
		log.debug("commit()");
		commitCalled = Boolean.TRUE;

		for (final SolrInputDocument document : docs) {
			String json = "";
			try {
				json = SolrDocumentConverter.toJsonString(document);
			} catch (final Exception e) {
				throw new WriterException("Can not convert document into JSON for index", e);
			}
			processLogger.logDocument(json.toString());
			log.debug("json: " + json);

			try {
				HttpClient.httpPost(baseUrl, json);
			} catch (final Exception e) {
				throw new WriterException("Can not post document index", e);
			}
		}

		/*
		SolrInputField idField = document.get("id");
		if (idField != null) {
			Object idObj = idField.getFirstValue();
			String id = "";
			if (idObj.getClass().equals(Long.class)) {
				Long idLong = (Long) idObj;
				id = idLong.toString();
			} else {
				id = (String) idObj;
			}
		}*/
	}

	@Override
	public void close() {
		log.debug("close()");
		//super.close();
	}

	@Override
	public void rollback() {
		log.debug("rollback()");
		//super.rollback();
	}
}