package com.strandls.naksha.binning.models;

import java.util.Map;

/**
 * A feature in {@link Geojson}
 * @author mukund
 *
 */
public class Feature {

	private final String type = "Feature";
	
	private Geometry geometry;
	
	private Map<String, Object> properties;

	public Feature(Geometry geometry, Map<String, Object> properties) {
		super();
		this.geometry = geometry;
		this.properties = properties;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getType() {
		return type;
	}
}
