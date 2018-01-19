package com.strandls.naksha.es.models;

/**
 * A response to the query request made to map app
 * 
 * @author mukund
 */
public class MapQueryResponse {

	private MapQueryStatus result;

	private String message;

	public MapQueryResponse(MapQueryStatus result, String message) {
		super();
		this.result = result;
		this.message = message;
	}

	public MapQueryStatus getResult() {
		return result;
	}

	public void setResult(MapQueryStatus result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
