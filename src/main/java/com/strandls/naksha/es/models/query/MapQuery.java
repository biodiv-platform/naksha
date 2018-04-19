package com.strandls.naksha.es.models.query;

/**
 * All Map queries extend this class
 * @author mukund
 *
 */
public class MapQuery {

	/**
	 * The field key
	 */
	private String key;
	/**
	 * The path where this field is present,
	 * can be <code>null</code>
	 */
	private String path;
	
	public MapQuery() {}

	public MapQuery(String key, String path) {
		this.key = key;
		this.path = path;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
