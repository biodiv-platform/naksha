package com.strandls.naksha.es.models;

/**
 * A response to the request made to map app
 * 
 * @author mukund
 */
public class MapResponse {

	public MapQueryStatus result;

	public String message;

	public MapResponse(MapQueryStatus result, String message) {
		super();
		this.result = result;
		this.message = message;
	}
}
