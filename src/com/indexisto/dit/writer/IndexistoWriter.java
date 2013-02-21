package com.indexisto.dit.writer;

import org.apache.solr.handler.dataimport.writer.SolrWriter;

import com.indexisto.dit.data.logger.DihProcessLogger;

public class IndexistoWriter extends SolrWriter {

    protected DihProcessLogger processLogger;

    public IndexistoWriter() {
        super();
    }

    public DihProcessLogger getProcessLogger() {
        return processLogger;
    }

    public void setProcessLogger(DihProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

}