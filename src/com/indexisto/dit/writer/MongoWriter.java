package com.indexisto.dit.writer;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indexisto.dit.DataImportHandler;
import com.indexisto.dit.data.exception.WriterException;
import com.indexisto.dit.helper.SolrDocumentConverter;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoWriter extends IndexistoWriter {

    private DBCollection collection;

    Boolean deleteAllCalled;
    Boolean commitCalled;
    List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    private static Logger log = LoggerFactory.getLogger(ElasticWriter.class);

    public MongoWriter(String domain, int port, String index,
            String entity, String charset) {
        super();

        try {
            final Mongo mongo = new Mongo(domain, port);
            final DB db = mongo.getDB(DataImportHandler.MONGO_DB_NAME);
            collection = db.getCollection(index);
        } catch (final Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public boolean upload(SolrInputDocument doc) {
        log.debug("upload()");
        return docs.add(doc);
    }

    @Override
    public void doDeleteAll() {
        log.debug("doDeleteAll()");
        deleteAllCalled = Boolean.TRUE;
    }

    @Override
    public void commit(boolean b) {
        log.debug("commit()");
        commitCalled = Boolean.TRUE;

        for (final SolrInputDocument document : docs) {
            BasicDBObject object = new BasicDBObject();
            try {
                object = SolrDocumentConverter.toMongoObject(document);
            } catch (final Exception e) {
                throw new WriterException("Can not convert document into JSON for index", e);
            }
            processLogger.logDocument(object.toString());
            log.debug("json: " + object);

            try {
                collection.insert(object);
            } catch (final Exception e) {
                throw new WriterException("Can not post document index", e);
            }
        }
    }

    @Override
    public void close() {
        log.debug("close()");
        //super.close();
    }

    @Override
    public void rollback() {
        log.debug("rollback()");
        //super.rollback();
    }
}