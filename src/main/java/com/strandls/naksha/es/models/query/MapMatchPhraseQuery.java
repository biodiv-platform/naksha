package com.strandls.naksha.es.models.query;

/**
 * This is to support elastic search match phrase query
 * 
 * @author mukund
 *
 */
public class MapMatchPhraseQuery extends MapQuery {

	private Object value;

	public MapMatchPhraseQuery() {
	}

	public MapMatchPhraseQuery(String key, Object value, String path) {
		super(key, path);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValues(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MapBoolQuery [key=" + getKey() + ", value=" + value + ", path=" + getPath() + "]";
	}

}
