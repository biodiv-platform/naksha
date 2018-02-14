package com.strandls.naksha.es.models.query;

import java.util.List;

/**
 * Query of the form that a key can have multiple values
 * 
 * @author mukund
 */
public class MapBoolQuery {

	private String key;

	private List<Object> values;

	public MapBoolQuery() {}
	
	public MapBoolQuery(String key, List<Object> values) {
		super();
		this.key = key;
		this.values = values;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "MapBoolQuery [key=" + key + ", values=" + values + "]";
	}
}
