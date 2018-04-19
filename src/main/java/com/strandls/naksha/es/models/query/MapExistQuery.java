package com.strandls.naksha.es.models.query;

/**
 * Query of the form that a key exists/not exists with a not null value
 * 
 * @author mukund
 *
 */
public class MapExistQuery extends MapQuery {

	private boolean exists;

	public MapExistQuery() {}
	
	public MapExistQuery(String key, boolean exists, String path) {
		super(key, path);
		this.exists = exists;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	@Override
	public String toString() {
		return "MapExistQuery [key=" + getKey() + ", exists=" + exists + ", path=" + getPath() + "]";
	}
}
