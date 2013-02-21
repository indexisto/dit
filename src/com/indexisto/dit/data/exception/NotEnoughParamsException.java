package com.indexisto.dit.data.exception;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

public class NotEnoughParamsException extends DataImportHandlerException {

    public NotEnoughParamsException() {
        super(SEVERE);
    }

    public NotEnoughParamsException(String message) {
        super(SEVERE, message);
    }

    public NotEnoughParamsException(Throwable cause) {
        super(SEVERE, cause);
    }

    public NotEnoughParamsException(String message, Throwable cause) {
        super(SEVERE, message, cause);
    }
}