package com.indexisto.dit.experimental.evaluator;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEvaluator extends Evaluator {

	private static Logger log = LoggerFactory.getLogger(TestEvaluator.class);
	
	@Override
	public String evaluate(String expression, Context context) {
		log.info("expression: " + expression);
	    return ""; //expression.toUpperCase();
	}
}