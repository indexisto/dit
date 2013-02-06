package com.indexisto.dit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.RequestInfo;
import org.apache.solr.handler.dataimport.SolrWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.writer.ElasticWriter;


public class DataImportTool {

	private static Logger log = LoggerFactory.getLogger(DataImportTool.class);

	//-Djava.util.logging.config.file=logging.properties
	public static void main(String[] args) {		
		SolrWriter writer = getTestWriter();
		String mapping = getTestMapping();		
		Map<String, Object> params = getTestParams();
		
		log.info("Starting DIT test session");
		try {			
			snatch(mapping, writer, params);
		} catch (DataSourceException e) {
			log.error("Data source problem (check snatcher settings)!");
		} catch (WriterException e) {
			log.error("Writer problem (check writer settings)!");	
		} catch (DataImportHandlerException e) {
			log.error("DataImportHandlerException", e);
		} catch (Exception e) {
			log.error("UNKNOWN PROBLEM", e);
		}		
	}

	public static void snatch(String mapping, SolrWriter writer,
			Map<String, Object> params) throws DataSourceException, WriterException {
		DataImporter di = new DataImporter();
		di.loadAndInit(mapping);
		RequestInfo requestInfo = new RequestInfo(params, null);
		di.runCmd(requestInfo, writer);
	}
	
	private static SolrWriter getTestWriter() {
		return new ElasticWriter("localhost", 9200, "test", "document", "UTF-8");
	}

	private static Map<String, Object> getTestParams() {
		Map<String, Object> dataImportParams = new LinkedHashMap<String, Object>();
		dataImportParams.put("command", "full-import");
		dataImportParams.put("clean", "true");
		dataImportParams.put("commit", "true");
		dataImportParams.put("entity", "forum_message");
		dataImportParams.put("datasource", "com.indexisto.dit.datasource.SnatcherDataSource");
		Map<String, Object> dataSourceParams = new HashMap<String, Object>();
		dataSourceParams.put("snatcherUrl", "http://46.4.39.138:8082/snatcher.php");
		dataImportParams.put("dataSourceParams", dataSourceParams);
		return dataImportParams;
	}	
	
	private static String getTestMapping() {
		String conf = "<dataConfig>"
		+ "<script><![CDATA[	"
		+ " function ciu(row, context) {	"
		+ "    		var forumId = row.get('forum_id');	"
		+ "			forumId += ' script test';	"
		+ "    		row.put('forum_id', forumId);	"
		+ "    		return row;	"
		+ "	} "
		+ "]]></script> "	
		+ "<document>"
		+ "		<entity name=\"forum_message\" "
		+ " 	 transformer=\"script:ciu\"  "				
		+ "		 query=\"SELECT "
		+ "		 id, forum_id, topic_id, attach_img, " 
		+ "		 post_date, post_message, post_message_html, "
		+ "		 author_name "
		+ "      FROM b_forum_message WHERE approved='Y'\">"
		+ "			<field column=\"id\" name=\"id\" /> "
		+ "			<field column=\"forum_id\" name=\"forum_id\" />"
		+ "			<field column=\"post_date\" name=\"post_date\" />"	
		+ "			<field column=\"post_message\" name=\"post_message\" />"				
		+ "		</entity>"
		+ "</document>" 
		+ "</dataConfig>";
		return conf;
	}	
}

/*+ "<dataSource type=\"JdbcDataSource\" " 
+ "driver=\"com.mysql.jdbc.Driver\" "
+ "url=\"jdbc:mysql://localhost/bitrix\" " 
+ "user=\"root\" " 
+ "password=\"s2s\"/> "*/