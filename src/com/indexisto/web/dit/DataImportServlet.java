package com.indexisto.web.dit;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

import com.indexisto.dit.DataImportTool;
import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.data.logger.ImportProcessLogger;
import com.indexisto.dit.data.logger.LogRecord;
import com.indexisto.dit.writer.ElasticWriter;

public class DataImportServlet extends HttpServlet{

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		final String eshost = request.getParameter("eshost");
		final String esport = request.getParameter("esport");
		final String esindex = request.getParameter("esindex");
		final String snatcher = request.getParameter("snatcher");
		final String mapping = request.getParameter("mapping");
		final String entity = request.getParameter("entity");


        final ImportProcessLogger processLogger = new ImportProcessLogger();

		final ElasticWriter writer = new ElasticWriter(eshost,
				Integer.parseInt(esport), esindex, entity, "UTF-8", processLogger);

		final Map<String, Object> dataSourceParams = new HashMap<String, Object>();
		dataSourceParams.put(DataImportTool.SNATCHER_URL, snatcher);
		final Map<String, Object> dataImportParams = new LinkedHashMap<String, Object>();
		dataImportParams.put("command", "full-import");
		dataImportParams.put("clean", "true");
		dataImportParams.put("commit", "true");
		dataImportParams.put("entity", entity);
		dataImportParams.put("datasource", "com.indexisto.dit.datasource.SnatcherDataSource");
		dataImportParams.put("dataSourceParams", dataSourceParams);
		dataImportParams.put(DataImportTool.PROCESS_LOGGER, processLogger);

		String result = "Data import finished successfully";
		try {
			DataImportTool.snatch(mapping, writer, dataImportParams);
		} catch (final DataSourceException e) {
			result = "Data source problem (check snatcher settings)!";
		} catch (final WriterException e) {
			result = "Writer problem (check writer settings)!";
        } catch (final MappingException e) {
            result = "Mapping problem (check your mapping)!";
		} catch (final DataImportHandlerException e) {
			result = "UNKNOWN Data Import Handler PROBLEM!";
		} catch (final Exception e) {
			result = "UNKNOWN PROBLEM";
		}

		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - SSS");
		response.setContentType("text/html; UTF-8");
		response.setCharacterEncoding("UTF-8");
		final PrintWriter out = response.getWriter();
		out.println("<html><head></head><body>");
		out.println(result);

		out.println("<hr><hr><hr>");
        final List<LogRecord> sqlRequests = processLogger.getSqlRequests();
        for(final LogRecord record : sqlRequests) {
            out.println("----- SQL запрос ----- " + sdf.format(record.getTime()) + " -----</br>");
            out.println(record.getMessage() + "</br></br>");
        }

        out.println("<hr><hr><hr>");
        final List<LogRecord> sqlResponses = processLogger.getSqlResponses();
        for(final LogRecord record : sqlResponses) {
            out.println("----- SQL ответ ----- " + sdf.format(record.getTime()) + " -----</br>");
            out.println(record.getMessage() + "</br></br>");
        }

        out.println("<hr><hr><hr>");
        final List<LogRecord> documents = processLogger.getDocuments();
        for(final LogRecord record : documents) {
            out.println("----- Документ ----- " + sdf.format(record.getTime()) + " -----</br>");
            out.println(record.getMessage() + "</br></br>");
        }

        out.println("<body></html>");
	}
}