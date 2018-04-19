package com.strandls.naksha.es.models.query;

import java.util.List;

/**
 * There is "AND" between any two instances of this query.
 * @author mukund
 *
 */
public class MapAndBoolQuery extends MapBoolQuery {

	public MapAndBoolQuery() {
		super();
	}

	public MapAndBoolQuery(String key, List<Object> values) {
		this(key, values, null);
	}

	public MapAndBoolQuery(String key, List<Object> values, String path) {
		super(key, values, path);
	}

}
