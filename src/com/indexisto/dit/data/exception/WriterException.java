package com.indexisto.dit.data.exception;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

public class WriterException extends DataImportHandlerException {
	
	public WriterException() {
		super(SEVERE);
	}

	public WriterException(String message) {
		super(SEVERE, message);
	}

	public WriterException(Throwable cause) {
		super(SEVERE, cause);
	}

	public WriterException(String message, Throwable cause) {
		super(SEVERE, message, cause);
	}
}