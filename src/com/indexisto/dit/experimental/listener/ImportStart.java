package com.indexisto.dit.experimental.listener;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportStart implements EventListener {

	private static Logger log = LoggerFactory.getLogger(ImportStart.class);
	
	@Override
	public void onEvent(Context context) {
		log.info("import started");		
	}
	
}
