package com.indexisto.dit.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Util {
	
	private static Logger log = Logger.getLogger(HttpClient.class);
	
	@SuppressWarnings("unchecked")
	public static Map createMap(Object... args) {
		Map result = new LinkedHashMap();
		if (args == null || args.length == 0)
			return result;
		for (int i = 0; i < args.length - 1; i += 2)
			result.put(args[i], args[i + 1]);
		return result;
	}
	
	public static List<Map<String, Object>> mapJsonArray(String arrayJsonString) throws JSONException {
		List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
		
		JSONArray jsonArray = new JSONArray(arrayJsonString);
		
		for (int i = 0; i < jsonArray.length(); i++) {
			Map<String, Object> responseRow = new HashMap<String, Object>();
			JSONObject row = jsonArray.getJSONObject(i);
			Iterator<String> keys = row.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				responseRow.put(key, row.get(key));
			}
			response.add(responseRow);
		}
		
		return response;
	}
	
}