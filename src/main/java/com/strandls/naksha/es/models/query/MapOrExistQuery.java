package com.strandls.naksha.es.models.query;

/**
 * There is "OR" between any two instances of this query.
 * @author mukund
 *
 */
public class MapOrExistQuery extends MapExistQuery {

	public MapOrExistQuery() {
		super();
	}
	
	public MapOrExistQuery(String key, boolean exists) {
		super(key, exists);
	}

}
