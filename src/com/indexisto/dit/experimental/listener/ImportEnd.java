package com.indexisto.dit.experimental.listener;

import java.util.Iterator;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportEnd implements EventListener {

	private static Logger log = LoggerFactory.getLogger(ImportEnd.class);
	
	@Override
	public void onEvent(Context context) {
		log.info("import ended");	
		
		Map<String, Object> stats = context.getStats();
		Iterator<String> iterator = stats.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			log.info("import stats: " + key + " -> " + stats.get(key));
		}		
	}
	
}