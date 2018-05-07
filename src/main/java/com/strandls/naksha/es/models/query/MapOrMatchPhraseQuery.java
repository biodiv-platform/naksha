package com.strandls.naksha.es.models.query;

/**
 * There is "OR" between any two instances of this query.
 * @author mukund
 *
 */
public class MapOrMatchPhraseQuery extends MapMatchPhraseQuery {

	public MapOrMatchPhraseQuery() {
		super();
	}

	public MapOrMatchPhraseQuery(String key, Object value) {
		this(key, value, null);
	}

	public MapOrMatchPhraseQuery(String key, Object value, String path) {
		super(key, value, path);
	}

}
