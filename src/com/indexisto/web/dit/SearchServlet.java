package com.indexisto.web.dit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.indexisto.dit.helper.HttpClient;

public class SearchServlet extends HttpServlet{
	
   public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
	  
	  String query = request.getParameter("query");
	  String ajaxSearchResult = HttpClient.httpGet("http://localhost:9200/index/document/_search?q=id:" + query);
	  
	  JSONArray result = new JSONArray();	  
	  JSONObject hitsObj = new JSONObject();
	  try {
		  JSONObject respObj = new JSONObject(ajaxSearchResult);
		  if (respObj != null && !respObj.isNull("hits")) {
			  hitsObj = (JSONObject) respObj.get("hits");
			  if (hitsObj != null) {
				  JSONArray hits = hitsObj.getJSONArray("hits");
				  if (hits != null) {
					  for (int i = 0; i < hits.length(); i++) {
						  JSONObject hit = (JSONObject) hits.get(i);
						  JSONObject source = hit.getJSONObject("_source");
						  result.put(source);
					  }
				  }	  
			  } 
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
	  }

	  response.setContentType("application/json");
	  PrintWriter out = response.getWriter();
	  out.println(result.toString());
   }
}