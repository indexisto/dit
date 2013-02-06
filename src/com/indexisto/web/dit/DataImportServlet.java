package com.indexisto.web.dit;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

import com.indexisto.dit.DataImportTool;
import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.writer.ElasticWriter;

public class DataImportServlet extends HttpServlet{

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String eshost = request.getParameter("eshost");
		String esport = request.getParameter("esport");
		String esindex = request.getParameter("esindex");
		String snatcher = request.getParameter("snatcher");
		String mapping = request.getParameter("mapping");
		String entity = request.getParameter("entity");

		ElasticWriter writer = new ElasticWriter(eshost,
				Integer.parseInt(esport), esindex, entity, "UTF-8");

		Map<String, Object> dataSourceParams = new HashMap<String, Object>();
		dataSourceParams.put("snatcherUrl", snatcher);		
		Map<String, Object> dataImportParams = new LinkedHashMap<String, Object>();
		dataImportParams.put("command", "full-import");
		dataImportParams.put("clean", "true");
		dataImportParams.put("commit", "true");
		dataImportParams.put("entity", entity);
		dataImportParams.put("datasource", "com.indexisto.dit.datasource.SnatcherDataSource");
		dataImportParams.put("dataSourceParams", dataSourceParams);

		String result = "Data import finished successfully";
		try {			
			DataImportTool.snatch(mapping, writer, dataImportParams);
		} catch (DataSourceException e) {
			result = "Data source problem (check snatcher settings)!";
		} catch (WriterException e) {
			result = "Writer problem (check writer settings)!";	
		} catch (DataImportHandlerException e) {
			result = "UNKNOWN Data Import Handler PROBLEM!";	
		} catch (Exception e) {
			result = "UNKNOWN PROBLEM";
		}

		PrintWriter out = response.getWriter();
		out.println(result);
	}
}