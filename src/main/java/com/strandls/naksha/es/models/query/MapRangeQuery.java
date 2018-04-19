package com.strandls.naksha.es.models.query;

/**
 * Query of the form that a key can have its value in the range from start to
 * end both inclusive
 * 
 * @author mukund
 */
public class MapRangeQuery extends MapQuery {

	private Object start;

	private Object end;

	public MapRangeQuery() {}
	
	public MapRangeQuery(String key, Object start, Object end, String path) {
		super(key, path);
		this.start = start;
		this.end = end;
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

	@Override
	public String toString() {
		return "MapRangeQuery [key=" + getKey() + ", start=" + start + ", end=" + end + ", path=" + getPath() + "]";
	}
}
