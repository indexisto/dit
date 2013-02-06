package com.indexisto.dit.experimental.processor;

import java.util.Map;

import org.apache.solr.handler.dataimport.SqlEntityProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor3 extends SqlEntityProcessor {

	private static Logger log = LoggerFactory.getLogger(TestProcessor3.class);

	String collector = "";
	
	@Override
	protected Map<String, Object> getNext() {
		Map<String, Object> map = super.getNext();
		
		if (map != null) {
			String postText = (String) map.get("post_text");
			collector += " -> " + postText;
			log.info("test processor 3: " + collector);
		} else {
			collector = "";
			log.info("test processor 3: END");
		}
		return map;
	}	
}