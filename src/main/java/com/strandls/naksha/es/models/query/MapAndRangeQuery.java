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
		super(key, start, end);
	}

}
