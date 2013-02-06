package com.indexisto.dit.experimental.processor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.solr.handler.dataimport.SqlEntityProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor extends SqlEntityProcessor {

	private static Logger log = LoggerFactory.getLogger(TestProcessor.class);

	@Override
	protected Map<String, Object> getNext() {
		Map<String, Object> map = super.getNext();
		if (map != null) {
			Set<String> keys = map.keySet();
			if (keys != null) {
				Iterator<String> iterator = keys.iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					log.info("test processor: " + key + " -> " + map.get(key));
				}
			}	
		}	
		return map;
	}
	
}