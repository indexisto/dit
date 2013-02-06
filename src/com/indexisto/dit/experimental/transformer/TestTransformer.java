package com.indexisto.dit.experimental.transformer;

import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.experimental.evaluator.TestEvaluator;

public class TestTransformer extends Transformer {

	private static Logger log = LoggerFactory.getLogger(TestEvaluator.class);
	
	@Override
	public Object transformRow(Map<String, Object> row, Context context) {
		String name1 = (String) row.get("name1");  
		if (name1 != null) name1 += "(transformed)"; 
		row.put("name1", name1);
		return row;
	}	
}