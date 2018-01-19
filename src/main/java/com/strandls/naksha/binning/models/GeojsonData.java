package com.strandls.naksha.binning.models;

/**
 * A wrapper around {@link Geojson} with max and min counts of property value
 * 
 * @author mukund
 *
 */
public class GeojsonData {

	/**
	 * {@link Geojson}
	 */
	private Geojson geojson;
	/**
	 * Max count of a value in all features
	 */
	private long max_count;
	/**
	 * Min count of a value in all features
	 */
	private long min_count;

	public GeojsonData(Geojson geojson, long max_count, long min_count) {
		super();
		this.geojson = geojson;
		this.max_count = max_count;
		this.min_count = min_count;
	}

	public Geojson getGeojson() {
		return geojson;
	}

	public void setGeojson(Geojson geojson) {
		this.geojson = geojson;
	}

	public long getMax_count() {
		return max_count;
	}

	public void setMax_count(long max_count) {
		this.max_count = max_count;
	}

	public long getMin_count() {
		return min_count;
	}

	public void setMin_count(long min_count) {
		this.min_count = min_count;
	}

}
