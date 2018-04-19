package com.strandls.naksha.es.models.query;

/**
 * There is "AND" between any two instances of this query.
 * @author mukund
 *
 */
public class MapAndRangeQuery extends MapRangeQuery {

	public MapAndRangeQuery() {
		super();
	}

	public MapAndRangeQuery(String key, Object start, Object end) {
		this(key, start, end, null);
	}

	public MapAndRangeQuery(String key, Object start, Object end, String path) {
		super(key, start, end, path);
	}

}
