package com.indexisto.dit.data.exception;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

public class MappingException extends DataImportHandlerException {

    public MappingException() {
        super(SEVERE);
    }

    public MappingException(String message) {
        super(SEVERE, message);
    }

    public MappingException(Throwable cause) {
        super(SEVERE, cause);
    }

    public MappingException(String message, Throwable cause) {
        super(SEVERE, message, cause);
    }
}