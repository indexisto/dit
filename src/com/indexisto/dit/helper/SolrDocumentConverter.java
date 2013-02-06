package com.indexisto.dit.helper;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrDocumentConverter {
	
	private static Logger log = LoggerFactory.getLogger(HttpClient.class);

    public static String toJsonString(SolrInputDocument document) throws JSONException {
		JSONObject docJson = new JSONObject();
		
		for (SolrInputField field : document) {
			if (field.getValueCount() > 1 ) {
				Collection<Object> values = field.getValues();
				JSONArray valuesJsonArray = new JSONArray();
				for (Object value : values) {
					valuesJsonArray.put(value);
				}
				docJson.put(field.getName(), valuesJsonArray);	
			} else if (field.getValueCount() == 1) {
				String value = field.getValue().toString();
				docJson.put(field.getName(), value);
			}
		}
		
		return docJson.toString();
    }
}