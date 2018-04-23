package com.strandls.naksha.es.services.impl;

import java.util.List;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;

import com.strandls.naksha.es.models.query.MapAndBoolQuery;
import com.strandls.naksha.es.models.query.MapAndRangeQuery;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapExistQuery;
import com.strandls.naksha.es.models.query.MapOrBoolQuery;
import com.strandls.naksha.es.models.query.MapOrRangeQuery;
import com.strandls.naksha.es.models.query.MapQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;

public class ElasticSearchQueryUtil {

	private QueryBuilder getNestedQueryBuilder(MapQuery query, QueryBuilder queryBuilder) {
		if(query.getPath() == null)
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
		RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd());
		return query.getPath() != null ? getNestedQueryBuilder(query, queryBuilder) : queryBuilder;
	}

	private void buildBoolQueries(List<MapAndBoolQuery> andQueries, List<MapOrBoolQuery> orQueries, BoolQueryBuilder masterBoolQuery) {
		
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

	private void buildRangeQueries(List<MapAndRangeQuery> andQueries, List<MapOrRangeQuery> orQueries, BoolQueryBuilder masterBoolQuery) {
		
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

	protected BoolQueryBuilder getBoolQueryBuilder(MapSearchQuery searchQuery) {

		BoolQueryBuilder masterBoolQuery = QueryBuilders.boolQuery();
		if(searchQuery == null)
			return masterBoolQuery;

		buildBoolQueries(searchQuery.getAndBoolQueries(), searchQuery.getOrBoolQueries(), masterBoolQuery);
		buildRangeQueries(searchQuery.getAndRangeQueries(), searchQuery.getOrRangeQueries(), masterBoolQuery);
		buildExistsQueries(searchQuery.getAndExistQueries(), masterBoolQuery);
		
		return masterBoolQuery;
	}

	protected GeoGridAggregationBuilder getGeoGridAggregationBuilder(String field, Integer precision) {
		GeoGridAggregationBuilder geohashGrid = AggregationBuilders
				.geohashGrid(field + "-" + precision);
		geohashGrid.field(field);
		geohashGrid.precision(precision);
		return geohashGrid;
	}
}