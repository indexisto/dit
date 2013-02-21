package com.indexisto.web.dit;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

import com.indexisto.dit.DataImportHandler;
import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.data.logger.DihLogLevel;
import com.indexisto.dit.data.logger.DihLogRecord;
import com.indexisto.dit.data.logger.DihProcessLogger;
import com.indexisto.dit.data.object.DihCommandEnum;
import com.indexisto.dit.writer.ElasticWriter;
import com.indexisto.dit.writer.IndexistoWriter;

public class DataImportServlet extends HttpServlet{

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		final String eshost = request.getParameter("eshost");
		final String esport = request.getParameter("esport");
		final String esindex = request.getParameter("esindex");
		final String mapping = request.getParameter("mapping");
		final String entity = request.getParameter("entity");
		final String offset = request.getParameter("offset");
		final String limit = request.getParameter("limit");
		final String command = request.getParameter("command");
		final String lastTime = request.getParameter("lastTime");


        final IndexistoWriter writer = new ElasticWriter(eshost, Integer.parseInt(esport), esindex, entity, "UTF-8");

		String result = "<b>Data import finished successfully</b> <br>";
		try {
		    DihProcessLogger processLog = new DihProcessLogger(DihLogLevel.DEBUG);
            final DataImportHandler dih = new DataImportHandler(mapping, entity, writer, null, processLog);
            if (DihCommandEnum.getByName(command) == DihCommandEnum.FULL_IMPORT) {
                processLog = dih.doFullImport(Long.parseLong(offset), Long.parseLong(limit));
            }
            if (DihCommandEnum.getByName(command) == DihCommandEnum.DELTA_IMPORT) {
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date lastIndexTime = sdf.parse(lastTime);
                processLog = dih.doDeltaImport(Long.parseLong(offset), Long.parseLong(limit), lastIndexTime);
            }
		    if (processLog.getDocumentsCount() == 0) {
		        result = "<b>NOTHING IS IMPORTED</b> <br>";
		    }
		    result += processLog.toString();
		    printResult(result, processLog, request, response);
		} catch (final DataSourceException e) {
			result = "<b>Data source problem (check snatcher settings)!</b> <br>" + e.getMessage();
		} catch (final WriterException e) {
			result = "<b>Writer problem (check writer settings)!</b> <br>" + e.getMessage();
        } catch (final MappingException e) {
            result = "<b>Mapping problem (check your mapping)</b>! <br>" + e.getMessage();
		} catch (final DataImportHandlerException e) {
			result = "<b>UNKNOWN Data Import Handler PROBLEM!</b>";
		} catch (final NumberFormatException e) {
		    result = "<b>WRONG PARAMS: offset, limit ?</b> <br>";
        } catch (final ParseException e) {
            result = "<b>WRONG PARAMS: last import time ?</b> <br>";
		} catch (final Exception e) {
			result = "<b>UNKNOWN PROBLEM</b>" + e.toString();
		} finally {
		    printResult(result, null, request, response);
		}
	}

	private void printResult(String result, DihProcessLogger processLog,
	        HttpServletRequest request, HttpServletResponse response) throws IOException {
	       final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - SSS");
	        response.setContentType("text/html; UTF-8");
	        response.setCharacterEncoding("UTF-8");
	        final PrintWriter out = response.getWriter();
	        out.println("<html><head></head><body>");
	        out.println(result);

	        if (processLog != null && request.getParameter("requestsLog") != null) {
	            out.println("<hr><hr><hr>");
	            final List<DihLogRecord> sqlRequests = processLog.getSqlRequests();
	            for(final DihLogRecord record : sqlRequests) {
	                out.println("----- SQL Query ----- " + sdf.format(record.getTime()) + " -----</br>");
	                out.println(record.getMessage() + "</br></br>");
	            }
	        }

	        if (processLog != null && request.getParameter("responsesLog") != null) {
	            out.println("<hr><hr><hr>");
	            final List<DihLogRecord> sqlResponses = processLog.getSqlResponses();
	            for(final DihLogRecord record : sqlResponses) {
	                out.println("----- SQL Response ----- " + sdf.format(record.getTime()) + " -----</br>");
	                out.println(record.getMessage() + "</br></br>");
	            }
	        }

	        if (processLog != null && request.getParameter("documentsLog") != null) {
	            out.println("<hr><hr><hr>");
	            final List<DihLogRecord> documents = processLog.getDocuments();
	            for(final DihLogRecord record : documents) {
	                out.println("----- Document ----- " + sdf.format(record.getTime()) + " -----</br>");
	                out.println(record.getMessage() + "</br></br>");
	            }
	        }

	        out.println("<body></html>");
    }
}