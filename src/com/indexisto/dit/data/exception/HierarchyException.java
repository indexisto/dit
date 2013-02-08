package com.indexisto.dit.data.exception;

import org.apache.solr.handler.dataimport.DataImportHandlerException;

public class HierarchyException extends DataImportHandlerException {
	
	public HierarchyException() {
		super(SEVERE);
	}

	public HierarchyException(String message) {
		super(SEVERE, message);
	}

	public HierarchyException(Throwable cause) {
		super(SEVERE, cause);
	}

	public HierarchyException(String message, Throwable cause) {
		super(SEVERE, message, cause);
	}
}