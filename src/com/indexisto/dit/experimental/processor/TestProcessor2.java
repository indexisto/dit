package com.indexisto.dit.experimental.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.handler.dataimport.EntityProcessorBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor2 extends EntityProcessorBase {

	private static Logger log = LoggerFactory.getLogger(TestProcessor2.class);

	private int counter = 1;
	private int counter2 = 1;
	
	@Override
	public Map<String, Object> nextRow() {
		if (counter > 3) {	
			counter = 1;
			return null;
		}
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("id", counter);
		row.put("name" + counter, "test2 - " + counter2);
		counter++;
		counter2++;
		return row;
	}	
}