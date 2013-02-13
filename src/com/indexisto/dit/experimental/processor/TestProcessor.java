package com.indexisto.dit.experimental.processor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.solr.handler.dataimport.processor.SqlEntityProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor extends SqlEntityProcessor {

	private static Logger log = LoggerFactory.getLogger(TestProcessor.class);

	@Override
	protected Map<String, Object> getNext() {
		final Map<String, Object> map = super.getNext();
		if (map != null) {
			final Set<String> keys = map.keySet();
			if (keys != null) {
				final Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					final String key = iterator.next();
					log.info("test processor: " + key + " -> " + map.get(key));
				}
			}
		}
		return map;
	}

}