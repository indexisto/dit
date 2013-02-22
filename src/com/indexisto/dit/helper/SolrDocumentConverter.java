package com.indexisto.dit.helper;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;

public class SolrDocumentConverter {

	private static Logger log = LoggerFactory.getLogger(HttpClient.class);

    public static String toJsonString(SolrInputDocument document) throws JSONException {
		final JSONObject docJson = new JSONObject();

		for (final SolrInputField field : document) {
			if (field.getValueCount() > 1 ) {
				final Collection<Object> values = field.getValues();
				final JSONArray valuesJsonArray = new JSONArray();
				for (final Object value : values) {
					valuesJsonArray.put(value);
				}
				docJson.put(field.getName(), valuesJsonArray);
			} else if (field.getValueCount() == 1) {
				final String value = field.getValue().toString();
				docJson.put(field.getName(), value);
			}
		}

		return docJson.toString();
    }

    public static BasicDBObject toMongoObject(SolrInputDocument document) throws JSONException {
        final BasicDBObject doc = new BasicDBObject();

        for (final SolrInputField field : document) {
            if (field.getValueCount() > 1 ) {
                final Collection<Object> values = field.getValues();
                final JSONArray valuesJsonArray = new JSONArray();
                for (final Object value : values) {
                    valuesJsonArray.put(value);
                }
                doc.put(field.getName(), valuesJsonArray);
            } else if (field.getValueCount() == 1) {
                final String value = field.getValue().toString();
                doc.put(field.getName(), value);
            }
        }

        return doc;
    }
}