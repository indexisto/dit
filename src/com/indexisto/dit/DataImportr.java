package com.indexisto.dit;

import java.util.Calendar;
import java.util.Date;

import org.apache.solr.handler.dataimport.DataImportHandlerException;
import org.apache.solr.handler.dataimport.DataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.data.exception.DataSourceException;
import com.indexisto.dit.data.exception.MappingException;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.data.logger.DihProcessLogger;
import com.indexisto.dit.helper.Util;
import com.indexisto.dit.writer.IndexistoWriter;
import com.indexisto.dit.writer.MongoWriter;


public class DataImportr {

    private static Logger log = LoggerFactory.getLogger(DataImporter.class);

    //-Djava.util.logging.config.file=logging.properties
    public static void main(String[] args) {
        final String rootEntityName = "document";
        //final IndexistoWriter writer = new ElasticWriter("localhost", 9200, "test", rootEntityName, "UTF-8");
        //final IndexistoWriter writer = new ElasticWriter("localhost", 9200, "test", rootEntityName, "UTF-8");
        final IndexistoWriter writer = new MongoWriter("localhost", 27017, "test", rootEntityName, "UTF-8");

        //final String mapping = Util.readFileToString("resources/presets/bitrix-blog_post.xml");
        //final String mapping = Util.readFileToString("resources/presets/bitrix-iblock_element.xml");
        //final String mapping = Util.readFileToString("resources/presets/bitrix-blog_post.xml");
        final String mapping = Util.readFileToString("resources/presets/bitrix-iblock_element(recursion-test).xml");

        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_MONTH, 19);
        cal.set(Calendar.MONTH, 0);


        log.info("Starting DIT test session");
        try {
            final DataImportHandler dih = new DataImportHandler(mapping, rootEntityName, writer, null, null);
            final DihProcessLogger processLog = dih.doFullImport(0, 10);
            //processLog = dih.doFullImport(0, 110);
            //final DihProcessLogger processLog = dih.doDeltaImport(0, 110, cal.getTime());
            log.info(processLog.toString());
        } catch (final DataSourceException e) {
            log.error("Data source problem (check snatcher settings)! - " + e.getMessage());
        } catch (final WriterException e) {
            log.error("Writer problem (check writer settings)! - " + e.getMessage());
        } catch (final MappingException e) {
            log.error("Mapping problem (check your mapping)! - " + e.getMessage());
        } catch (final DataImportHandlerException e) {
            log.error("DataImportHandlerException", e);
        } catch (final Exception e) {
            log.error("UNKNOWN PROBLEM", e);
        }
    }
}