package com.strandls.naksha.es.models.query;

/**
 * Query of the form that a key exists/not exists with a not null value
 * 
 * @author mukund
 *
 */
public class MapExistQuery {

	private String key;
	
	private boolean exists;

	public MapExistQuery() {}
	
	public MapExistQuery(String key, boolean exists) {
		super();
		this.key = key;
		this.exists = exists;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	@Override
	public String toString() {
		return "MapExistQuery [key=" + key + ", exists=" + exists + "]";
	}
}
