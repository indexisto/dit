package com.indexisto.dit.data.exception;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

public class DataSourceException extends DataImportHandlerException {
	
	public DataSourceException() {
		super(SEVERE);
	}

	public DataSourceException(String message) {
		super(SEVERE, message);
	}

	public DataSourceException(Throwable cause) {
		super(SEVERE, cause);
	}

	public DataSourceException(String message, Throwable cause) {
		super(SEVERE, message, cause);
	}
}