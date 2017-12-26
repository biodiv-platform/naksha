package com.strandls.naksha.es.models;

/**
 * A single unit of user data used for the communication with the app.
 * 
 * @author mukund
 */
public class MapDocument {

	private Object document;

	// for json serialization/de-serialization
	public MapDocument() {
	}

	public MapDocument(Object document) {
		super();
		this.document = document;
	}

	public Object getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

}
