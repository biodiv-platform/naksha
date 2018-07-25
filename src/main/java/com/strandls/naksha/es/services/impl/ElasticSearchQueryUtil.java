package com.strandls.naksha.es.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoPolygonQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import com.strandls.naksha.es.models.MapBoundParams;
import com.strandls.naksha.es.models.MapBounds;
import com.strandls.naksha.es.models.MapGeoPoint;
import com.strandls.naksha.es.models.MapSearchParams;
import com.strandls.naksha.es.models.query.MapAndBoolQuery;
import com.strandls.naksha.es.models.query.MapAndMatchPhraseQuery;
import com.strandls.naksha.es.models.query.MapAndRangeQuery;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapExistQuery;
import com.strandls.naksha.es.models.query.MapMatchPhraseQuery;
import com.strandls.naksha.es.models.query.MapOrBoolQuery;
import com.strandls.naksha.es.models.query.MapOrMatchPhraseQuery;
import com.strandls.naksha.es.models.query.MapOrRangeQuery;
import com.strandls.naksha.es.models.query.MapQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;

public class ElasticSearchQueryUtil {

	private static final int SHARD_SIZE = 100;

	private QueryBuilder getNestedQueryBuilder(MapQuery query, QueryBuilder queryBuilder) {
		if (query.getPath() == null)
			return queryBuilder;
		return QueryBuilders.nestedQuery(query.getPath(), queryBuilder, ScoreMode.None);
	}

	private QueryBuilder getTermsQueryBuilder(MapBoolQuery query) {
		TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery(query.getKey(), query.getValues());
		return query.getPath() != null ? getNestedQueryBuilder(query, queryBuilder) : queryBuilder;
	}

	private QueryBuilder getExistsQueryBuilder(MapQuery query) {
		ExistsQueryBuilder queryBuilder = QueryBuilders.existsQuery(query.getKey());
		return query.getPath() != null ? getNestedQueryBuilder(query, queryBuilder) : queryBuilder;
	}

	private QueryBuilder getRangeQueryBuilder(MapRangeQuery query) {
		RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(query.getKey()).gte(query.getStart())
				.lte(query.getEnd());
		return query.getPath() != null ? getNestedQueryBuilder(query, queryBuilder) : queryBuilder;
	}

	private QueryBuilder getMatchPhraseQueryBuilder(MapMatchPhraseQuery query) {
		MatchPhraseQueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(query.getKey(), query.getValue());
		return query.getPath() != null ? getNestedQueryBuilder(query, queryBuilder) : queryBuilder;
	}

	private void buildBoolQueries(List<MapAndBoolQuery> andQueries, List<MapOrBoolQuery> orQueries,
			BoolQueryBuilder masterBoolQuery) {

		BoolQueryBuilder boolQuery;

		if (andQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapBoolQuery query : andQueries) {
				if (query.getValues() != null)
					boolQuery.must(getTermsQueryBuilder(query));
				else
					boolQuery.mustNot(getExistsQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (orQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapBoolQuery query : orQueries) {
				if (query.getValues() != null)
					boolQuery.should(getTermsQueryBuilder(query));
				else
					boolQuery.mustNot(getExistsQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}
	}

	private void buildRangeQueries(List<MapAndRangeQuery> andQueries, List<MapOrRangeQuery> orQueries,
			BoolQueryBuilder masterBoolQuery) {

		BoolQueryBuilder boolQuery;

		if (andQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapAndRangeQuery query : andQueries) {
				boolQuery.must(getRangeQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (orQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapOrRangeQuery query : orQueries) {
				boolQuery.should(getRangeQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}
	}

	private void buildExistsQueries(List<MapExistQuery> andExistQueries, BoolQueryBuilder masterBoolQuery) {

		if (andExistQueries != null) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			for (MapExistQuery query : andExistQueries) {
				if (query.isExists())
					boolQuery.must(getExistsQueryBuilder(query));
				else
					boolQuery.mustNot(getExistsQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}
	}

	private void buildMatchPhraseQueries(List<MapAndMatchPhraseQuery> andQueries, List<MapOrMatchPhraseQuery> orQueries,
			BoolQueryBuilder masterBoolQuery) {
		BoolQueryBuilder boolQuery;

		if (andQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapAndMatchPhraseQuery query : andQueries) {
				if (query.getValue() != null)
					boolQuery.must(getMatchPhraseQueryBuilder(query));
				else
					boolQuery.mustNot(getExistsQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (orQueries != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapOrMatchPhraseQuery query : orQueries) {
				if (query.getValue() != null)
					boolQuery.should(getMatchPhraseQueryBuilder(query));
				else
					boolQuery.mustNot(getExistsQueryBuilder(query));
			}
			masterBoolQuery.must(boolQuery);
		}

	}

	protected BoolQueryBuilder getBoolQueryBuilder(MapSearchQuery searchQuery) {

		BoolQueryBuilder masterBoolQuery = QueryBuilders.boolQuery();
		if (searchQuery == null)
			return masterBoolQuery;

		buildBoolQueries(searchQuery.getAndBoolQueries(), searchQuery.getOrBoolQueries(), masterBoolQuery);
		buildRangeQueries(searchQuery.getAndRangeQueries(), searchQuery.getOrRangeQueries(), masterBoolQuery);
		buildExistsQueries(searchQuery.getAndExistQueries(), masterBoolQuery);
		buildMatchPhraseQueries(searchQuery.getAndMatchPhraseQueries(), searchQuery.getOrMatchPhraseQueries(),
				masterBoolQuery);
		return masterBoolQuery;
	}

	protected GeoGridAggregationBuilder getGeoGridAggregationBuilder(String field, Integer precision) {
		if (field == null)
			return null;

		precision = precision != null ? precision : 1;
		GeoGridAggregationBuilder geohashGrid = AggregationBuilders.geohashGrid(field + "-" + precision);
		geohashGrid.field(field);
		geohashGrid.precision(precision);
		return geohashGrid;
	}

	protected TermsAggregationBuilder getTermsAggregationBuilder(String field, String subField, Integer size) {
		TermsAggregationBuilder builder = AggregationBuilders.terms(field);
		builder.field(field);

		if (subField != null)
			builder.subAggregation(AggregationBuilders.terms(subField).field(subField));

		builder.size(size);
		builder.shardSize(SHARD_SIZE);
		return builder;
	}

	protected void applyMapBounds(MapSearchParams searchParams, BoolQueryBuilder masterBoolQuery,
			String geoAggregationField) {

		MapBoundParams mapBoundParams = searchParams.getMapBoundParams();
		if (mapBoundParams == null)
			return;

		MapBounds bounds = mapBoundParams.getBounds();
		if (bounds != null) {
			applyMapBounds(bounds, masterBoolQuery, geoAggregationField);
		}

		List<MapGeoPoint> polygon = mapBoundParams.getPolygon();
		if (polygon != null && !polygon.isEmpty()) {
			List<GeoPoint> geoPoints = new ArrayList<>();
			for (MapGeoPoint point : polygon)
				geoPoints.add(new GeoPoint(point.getLat(), point.getLon()));

			GeoPolygonQueryBuilder setPolygon = QueryBuilders.geoPolygonQuery(geoAggregationField, geoPoints);
			masterBoolQuery.filter(setPolygon);
		}
	}

	protected void applyMapBounds(MapBounds bounds, BoolQueryBuilder masterBoolQuery,
			String geoAggregationField) {

		if (bounds != null) {
			GeoBoundingBoxQueryBuilder setCorners = QueryBuilders.geoBoundingBoxQuery(geoAggregationField)
					.setCorners(bounds.getTop(), bounds.getLeft(), bounds.getBottom(), bounds.getRight());
			masterBoolQuery.filter(setCorners);
		}
	}
}
