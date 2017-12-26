package com.strandls.naksha.es.models;

import java.util.List;

/**
 * A master query with combination of {@link MapBoolQuery} and
 * {@link MapRangeQuery} There is an "AND" between any queries.
 * 
 * @author mukund
 */
public class MapSearchQuery {

	public List<MapBoolQuery> boolQueries;

	public List<MapRangeQuery> rangeQueries;

	// for json serialization/de-serialization
	public MapSearchQuery() {
	}

	public MapSearchQuery(List<MapBoolQuery> boolQueries, List<MapRangeQuery> rangeQueries) {
		super();
		this.boolQueries = boolQueries;
		this.rangeQueries = rangeQueries;
	}

}
