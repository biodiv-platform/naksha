package com.strandls.naksha.es.models.query;

/**
 * There is "AND" between any two instances of this query.
 * @author mukund
 *
 */
public class MapAndExistQuery extends MapExistQuery {

	public MapAndExistQuery() {
		super();
	}

	public MapAndExistQuery(String key, boolean exists) {
		this(key, exists, null);
	}

	public MapAndExistQuery(String key, boolean exists, String path) {
		super(key, exists, path);
	}

}
