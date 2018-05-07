package com.strandls.naksha.es.models.query;

import java.util.List;

import com.strandls.naksha.es.models.MapSearchParams;

/**
 * A master query with combination of {@link MapBoolQuery},
 * {@link MapRangeQuery}, {@link MapExistQuery}
 * and {@link MapMatchPhraseQuery}.
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

	private List<MapAndMatchPhraseQuery> andMatchPhraseQueries;

	private List<MapOrMatchPhraseQuery> orMatchPhraseQueries;

	private MapSearchParams searchParams;

	public MapSearchQuery() {
	}

	public MapSearchQuery(List<MapAndBoolQuery> andBoolQueries, List<MapOrBoolQuery> orBoolQueries,
			List<MapAndRangeQuery> andRangeQueries, List<MapOrRangeQuery> orRangeQueries,
			List<MapExistQuery> andExistQueries, List<MapAndMatchPhraseQuery> andMatchPhraseQueries,
			List<MapOrMatchPhraseQuery> orMatchPhraseQueries) {
		this(andBoolQueries, orBoolQueries, andRangeQueries, orRangeQueries, andExistQueries,
				andMatchPhraseQueries, orMatchPhraseQueries, null);
	}

	public MapSearchQuery(List<MapAndBoolQuery> andBoolQueries, List<MapOrBoolQuery> orBoolQueries,
			List<MapAndRangeQuery> andRangeQueries, List<MapOrRangeQuery> orRangeQueries,
			List<MapExistQuery> andExistQueries, List<MapAndMatchPhraseQuery> andMatchPhraseQueries,
			List<MapOrMatchPhraseQuery> orMatchPhraseQueries, MapSearchParams searchParams) {
		super();
		this.andBoolQueries = andBoolQueries;
		this.orBoolQueries = orBoolQueries;
		this.andRangeQueries = andRangeQueries;
		this.orRangeQueries = orRangeQueries;
		this.andExistQueries = andExistQueries;
		this.andMatchPhraseQueries = andMatchPhraseQueries;
		this.orMatchPhraseQueries = orMatchPhraseQueries;
		this.searchParams = searchParams;
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

	public List<MapAndMatchPhraseQuery> getAndMatchPhraseQueries() {
		return andMatchPhraseQueries;
	}

	public void setAndMatchPhraseQueries(List<MapAndMatchPhraseQuery> andMatchPhraseQueries) {
		this.andMatchPhraseQueries = andMatchPhraseQueries;
	}

	public List<MapOrMatchPhraseQuery> getOrMatchPhraseQueries() {
		return orMatchPhraseQueries;
	}

	public void setOrMatchPhraseQueries(List<MapOrMatchPhraseQuery> orMatchPhraseQueries) {
		this.orMatchPhraseQueries = orMatchPhraseQueries;
	}

	public MapSearchParams getSearchParams() {
		return searchParams;
	}

	public void setSearchParams(MapSearchParams searchParams) {
		this.searchParams = searchParams;
	}

	@Override
	public String toString() {
		return "MapSearchQuery [andBoolQueries=" + andBoolQueries + ", orBoolQueries=" + orBoolQueries
				+ ", andRangeQueries=" + andRangeQueries + ", orRangeQueries=" + orRangeQueries + ", andExistQueries="
				+ andExistQueries + ", andMatchPhraseQueries=" + andMatchPhraseQueries + ", orMatchPhraseQueries="
				+ orMatchPhraseQueries + ", searchParams=" + searchParams.toString() + "]";
	}

}
