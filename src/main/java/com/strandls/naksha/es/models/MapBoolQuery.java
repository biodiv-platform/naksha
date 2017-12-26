package com.strandls.naksha.es.models;

import java.util.List;

/**
 * Query of the form that a key can have multiple values
 * 
 * @author mukund
 */
public class MapBoolQuery {

	public String key;

	public List<Object> values;

	// for json serialization/de-serialization
	public MapBoolQuery() {
	}

	public MapBoolQuery(String key, List<Object> values) {
		super();
		this.key = key;
		this.values = values;
	}

}
