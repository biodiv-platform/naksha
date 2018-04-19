package com.strandls.naksha.es.models.query;

/**
 * There is "OR" between any two instances of this query.
 * @author mukund
 *
 */
public class MapOrRangeQuery extends MapRangeQuery {

	public MapOrRangeQuery() {
		super();
	}
	
	public MapOrRangeQuery(String key, Object start, Object end) {
		this(key, start, end, null);
	}

	public MapOrRangeQuery(String key, Object start, Object end, String path) {
		super(key, start, end, path);
	}

}
