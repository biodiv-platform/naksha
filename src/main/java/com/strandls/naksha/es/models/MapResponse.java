package com.strandls.naksha.es.models;

import java.util.List;

/**
 * A response result to the request made to app.
 * @author mukund
 *
 */
public class MapResponse {

	/**
	 * List of {@link MapDocument} to be sent as response
	 * to the request
	 */
	private List<MapDocument> documents;
	
	/**
	 * Total number of documents which satisfied the request condition.
	 * This number may be different than the size of list if limit/offset
	 * were specified in the request.
	 */
	private long totalDocuments;
	
	/**
	 * Geographic aggregation of the result based on geohash.
	 * Maximum buckets are 10000.
	 */
	private String geohashAggregation;
	
	/**
	 * Geohash aggregation filtered by bounds of user screen provided.
	 */
	private String viewFilteredGeohashAggregation;

	public MapResponse(List<MapDocument> documents, long totalDocuments, String geohashAggregation) {
		this(documents, totalDocuments, geohashAggregation, null);
	}
	
	public MapResponse(List<MapDocument> documents, long totalDocuments, String geohashAggregation,
			String viewFilteredGeohashAggregation) {
		super();
		this.documents = documents;
		this.totalDocuments = totalDocuments;
		this.geohashAggregation = geohashAggregation;
		this.viewFilteredGeohashAggregation = viewFilteredGeohashAggregation;
	}

	public List<MapDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<MapDocument> documents) {
		this.documents = documents;
	}

	public long getTotalDocuments() {
		return totalDocuments;
	}

	public void setTotalDocuments(long totalDocuments) {
		this.totalDocuments = totalDocuments;
	}

	public String getGeohashAggregation() {
		return geohashAggregation;
	}

	public void setGeohashAggregation(String geohashAggregation) {
		this.geohashAggregation = geohashAggregation;
	} 
	
	public String getViewFilteredGeohashAggregation() {
		return viewFilteredGeohashAggregation;
	}
	
	public void setViewFilteredGeohashAggregation(String viewFilteredGeohashAggregation) {
		this.viewFilteredGeohashAggregation = viewFilteredGeohashAggregation;
	}
}
