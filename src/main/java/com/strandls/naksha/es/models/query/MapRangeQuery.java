package com.strandls.naksha.es.models.query;

/**
 * Query of the form that a key can have its value in the range from start to
 * end both inclusive
 * 
 * @author mukund
 */
public class MapRangeQuery {

	private String key;

	private Object start;

	private Object end;

	public MapRangeQuery() {}
	
	public MapRangeQuery(String key, Object start, Object end) {
		super();
		this.key = key;
		this.start = start;
		this.end = end;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getStart() {
		return start;
	}

	public void setStart(Object start) {
		this.start = start;
	}

	public Object getEnd() {
		return end;
	}

	public void setEnd(Object end) {
		this.end = end;
	}

}
