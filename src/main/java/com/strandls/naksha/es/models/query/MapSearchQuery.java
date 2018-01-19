package com.strandls.naksha.es.models.query;

import java.util.List;

/**
 * A master query with combination of {@link MapBoolQuery},
 * {@link MapRangeQuery} and {@link MapExistQuery}.
 * There is an "AND" between any pair of queries.
 * 
 * @author mukund
 */
public class MapSearchQuery {

	private List<MapAndBoolQuery> andBoolQueries;

	private List<MapOrBoolQuery> orBoolQueries;

	private List<MapAndRangeQuery> andRangeQueries;

	private List<MapOrRangeQuery> orRangeQueries;

	private List<MapExistQuery> andExistQueries;

	public MapSearchQuery() {
	}

	public MapSearchQuery(List<MapAndBoolQuery> andBoolQueries, List<MapOrBoolQuery> orBoolQueries,
			List<MapAndRangeQuery> andRangeQueries, List<MapOrRangeQuery> orRangeQueries,
			List<MapExistQuery> andExistQueries) {
		super();
		this.andBoolQueries = andBoolQueries;
		this.orBoolQueries = orBoolQueries;
		this.andRangeQueries = andRangeQueries;
		this.orRangeQueries = orRangeQueries;
		this.andExistQueries = andExistQueries;
	}

	public List<MapAndBoolQuery> getAndBoolQueries() {
		return andBoolQueries;
	}

	public void setAndBoolQueries(List<MapAndBoolQuery> andBoolQueries) {
		this.andBoolQueries = andBoolQueries;
	}

	public List<MapOrBoolQuery> getOrBoolQueries() {
		return orBoolQueries;
	}

	public void setOrBoolQueries(List<MapOrBoolQuery> orBoolQueries) {
		this.orBoolQueries = orBoolQueries;
	}

	public List<MapAndRangeQuery> getAndRangeQueries() {
		return andRangeQueries;
	}

	public void setAndRangeQueries(List<MapAndRangeQuery> andRangeQueries) {
		this.andRangeQueries = andRangeQueries;
	}

	public List<MapOrRangeQuery> getOrRangeQueries() {
		return orRangeQueries;
	}

	public void setOrRangeQueries(List<MapOrRangeQuery> orRangeQueries) {
		this.orRangeQueries = orRangeQueries;
	}

	public List<MapExistQuery> getAndExistQueries() {
		return andExistQueries;
	}

	public void setAndExistQueries(List<MapExistQuery> andExistQueries) {
		this.andExistQueries = andExistQueries;
	}

}
