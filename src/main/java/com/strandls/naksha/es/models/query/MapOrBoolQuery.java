package com.strandls.naksha.es.models.query;

import java.util.List;

/**
 * There is "OR" between any two instances of this query.
 * @author mukund
 *
 */
public class MapOrBoolQuery extends MapBoolQuery {

	public MapOrBoolQuery() {
		super();
	}

	public MapOrBoolQuery(String key, List<Object> values) {
		this(key, values, null);
	}

	public MapOrBoolQuery(String key, List<Object> values, String path) {
		super(key, values, path);
	}

}
