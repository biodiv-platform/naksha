package com.strandls.naksha.es.models.query;

import java.util.List;

/**
 * Query of the form that a key can have multiple values
 * 
 * @author mukund
 */
public class MapBoolQuery extends MapQuery {

	private List<Object> values;

	public MapBoolQuery() {}
	
	public MapBoolQuery(String key, List<Object> values, String path) {
		super(key, path);
		this.values = values;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "MapBoolQuery [key=" + getKey() + ", values=" + values + ", path=" + getPath() + "]";
	}

}
