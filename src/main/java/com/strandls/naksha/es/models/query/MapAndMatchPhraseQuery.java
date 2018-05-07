package com.strandls.naksha.es.models.query;

/**
 * There is "AND" between any two instances of this query.
 * @author mukund
 *
 */
public class MapAndMatchPhraseQuery extends MapMatchPhraseQuery {

	public MapAndMatchPhraseQuery() {
		super();
	}

	public MapAndMatchPhraseQuery(String key, Object value) {
		this(key, value, null);
	}

	public MapAndMatchPhraseQuery(String key, Object value, String path) {
		super(key, value, path);
	}

}
