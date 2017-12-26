package com.strandls.naksha.es.models;

/**
 * Query of the form that a key can have its value in the range from start to
 * end both inclusive
 * 
 * @author mukund
 */
public class MapRangeQuery {

	public String key;

	public Object start;

	public Object end;

	// for json serialization/de-serialization
	public MapRangeQuery() {
	}

	public MapRangeQuery(String key, Object start, Object end) {
		super();
		this.key = key;
		this.start = start;
		this.end = end;
	}

}
