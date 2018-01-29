package com.strandls.naksha.es.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse.ShardInfo;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.es.ESClientProvider;
import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.models.MapBounds;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryResponse;
import com.strandls.naksha.es.models.MapQueryStatus;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSortType;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapExistQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;
import com.strandls.naksha.es.services.api.ElasticSearchService;

/**
 * Implementation of {@link ElasticSearchService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchServiceImpl implements ElasticSearchService {

	private final ElasticSearchClient client = ESClientProvider.getClient();

	private final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#create(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse create(String index, String type, String documentId, String document) throws IOException {

		logger.info("Trying to create index: {}, type: {} & id: {}", index, type, documentId);

		IndexRequest request = new IndexRequest(index, type, documentId);
		request.source(document, XContentType.JSON);
		IndexResponse indexResponse = client.index(request);

		ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();

		String failureReason = "";

		if (shardInfo.getFailed() > 0) {

			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason += failure.reason() + ";";
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());

		logger.info("Created index: {}, type: {} & id: {} with status {}", index, type, documentId,
				queryStatus.toString());

		return new MapQueryResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#fetch(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapDocument fetch(String index, String type, String documentId) throws IOException {

		logger.info("Trying to fetch index: {}, type: {} & id: {}", index, type, documentId);

		GetRequest request = new GetRequest(index, type, documentId);
		GetResponse response = client.get(request);

		logger.info("Fetched index: {}, type: {} & id: {} with status {}", index, type, documentId,
				response.isExists());

		return new MapDocument(response.getSourceAsString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#update(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse update(String index, String type, String documentId, Map<String, Object> document)
			throws IOException {

		logger.info("Trying to update index: {}, type: {} & id: {}", index, type, documentId);

		UpdateRequest request = new UpdateRequest(index, type, documentId);
		request.doc(document);
		UpdateResponse updateResponse = client.update(request);

		ShardInfo shardInfo = updateResponse.getShardInfo();

		String failureReason = "";

		if (shardInfo.getFailed() > 0) {
			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason = failure.reason() + ";";
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(updateResponse.getResult().name());

		logger.info("Updated index: {}, type: {} & id: {} with status {}", index, type, documentId,
				queryStatus.toString());

		return new MapQueryResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#delete(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapQueryResponse delete(String index, String type, String documentId) throws IOException {

		logger.info("Trying to delete index: {}, type: {} & id: {}", index, type, documentId);

		DeleteRequest request = new DeleteRequest(index, type, documentId);
		DeleteResponse deleteResponse = client.delete(request);

		ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

		String failureReason = "";

		if (shardInfo.getFailed() > 0) {

			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason = failure.reason() + ";";
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(deleteResponse.getResult().name());

		logger.info("Deleted index: {}, type: {} & id: {} with status {}", index, type, documentId,
				queryStatus.toString());

		return new MapQueryResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#bulkUpload(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<MapQueryResponse> bulkUpload(String index, String type, String jsonArray) throws IOException {
		List<MapQueryResponse> responses = new ArrayList<>();

		logger.info("Trying to bulk upload index: {}, type: {}", index, type);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode[] jsons = null;
		try {
			jsons = mapper.readValue(jsonArray, JsonNode[].class);
		} catch (JsonParseException e) {
			logger.error("Json parsing exception while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Parsing Exception"));
		} catch (JsonMappingException e) {
			logger.error("Json mapping exception while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Mapping Exception"));
		}

		if (!responses.isEmpty())
			return responses;

		if (jsons != null && !jsons[0].has("id")) {
			logger.error("No id field specified while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapQueryResponse(MapQueryStatus.NO_ID, "No id field specified"));
		}

		if (!responses.isEmpty())
			return responses;

		BulkRequest request = new BulkRequest();

		for (JsonNode json : jsons)
			request.add(
					new IndexRequest(index, type, json.get("id").asText()).source(json.toString(), XContentType.JSON));

		BulkResponse bulkResponse = client.bulk(request);

		for (BulkItemResponse bulkItemResponse : bulkResponse) {

			String failureReason = "";
			MapQueryStatus queryStatus;

			if (bulkItemResponse.isFailed()) {
				failureReason = bulkItemResponse.getFailureMessage();
				queryStatus = MapQueryStatus.ERROR;
			} else {
				IndexResponse indexResponse = (IndexResponse) bulkItemResponse.getResponse();
				ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();

				if (shardInfo.getFailed() > 0) {

					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						failureReason += failure.reason() + ";";
					}
				}

				queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());
			}

			logger.info(" For index: {}, type: {}, bulk upload id: {}, the status is {}", index, type,
					bulkItemResponse.getId(), queryStatus);

			responses.add(new MapQueryResponse(queryStatus, failureReason));
		}

		return responses;
	}

	private MapResponse querySearch(String index, String type, QueryBuilder query, Integer from, Integer limit,
			String sortOn, MapSortType sortType, String geoAggregationField, Integer geoAggegationPrecision)
			throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		if (from != null)
			sourceBuilder.from(from);
		if (limit != null)
			sourceBuilder.size(limit);

		if (sortOn != null) {
			SortOrder sortOrder = sortType != null && MapSortType.ASC == sortType ? SortOrder.ASC : SortOrder.DESC;
			sourceBuilder.sort(sortOn, sortOrder);
		}

		if (geoAggregationField != null) {
			geoAggegationPrecision = geoAggegationPrecision != null ? geoAggegationPrecision : 1;
			sourceBuilder.aggregation(getGeoGridAggregationBuilder(geoAggregationField, geoAggegationPrecision));
		}

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits();

		for (SearchHit hit : searchResponse.getHits().getHits())
			result.add(new MapDocument(hit.getSourceAsString()));

		logger.info("Search completed with total hits: {}", totalHits);

		String aggregationString = null;
		if (geoAggregationField != null) {
			Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
			aggregationString = XContentHelper.toString(aggregation);
			logger.info("Aggregation search: {} completed", aggregation.getName());
		}

		return new MapResponse(result, totalHits, aggregationString);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#termSearch(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public MapResponse termSearch(String index, String type, String key, String value, Integer from, Integer limit,
			String sortOn, MapSortType sortType, String geoAggregationField, Integer geoAggegationPrecision)
			throws IOException {

		logger.info("Term search for index: {}, type: {}, key: {}, value: {}", index, type, key, value);
		QueryBuilder query = QueryBuilders.termQuery(key, value);
		if (value != null)
			query = QueryBuilders.termQuery(key, value);
		else
			query = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(key));

		return querySearch(index, type, query, from, limit, sortOn, sortType, geoAggregationField,
				geoAggegationPrecision);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#boolSearch(java.lang
	 * .String, java.lang.String, java.util.List, java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse boolSearch(String index, String type, List<MapBoolQuery> queries, Integer from, Integer limit,
			String sortOn, MapSortType sortType, String geoAggregationField, Integer geoAggegationPrecision)
			throws IOException {

		logger.info("Bool search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapBoolQuery query : queries) {
			if (query.getValues() != null)
				boolQuery.must(QueryBuilders.termsQuery(query.getKey(), query.getValues()));
			else
				boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
		}

		return querySearch(index, type, boolQuery, from, limit, sortOn, sortType, geoAggregationField,
				geoAggegationPrecision);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#rangeSearch(java.
	 * lang.String, java.lang.String, java.util.List, java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse rangeSearch(String index, String type, List<MapRangeQuery> queries, Integer from, Integer limit,
			String sortOn, MapSortType sortType, String geoAggregationField, Integer geoAggegationPrecision)
			throws IOException {

		logger.info("Range search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapRangeQuery query : queries) {
			boolQuery.must(QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd()));
		}

		return querySearch(index, type, boolQuery, from, limit, sortOn, sortType, geoAggregationField,
				geoAggegationPrecision);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#search(java.lang.
	 * String, java.lang.String, com.strandls.naksha.es.models.query.MapSearchQuery,
	 * java.lang.Integer, java.lang.Integer, java.lang.String,
	 * com.strandls.naksha.es.models.MapSortType, java.lang.String,
	 * java.lang.Integer, java.lang.Boolean, com.strandls.naksha.es.models.MapBounds)
	 */
	@Override
	public MapResponse search(String index, String type, MapSearchQuery searchQuery, Integer from, Integer limit,
			String sortOn, MapSortType sortType, String geoAggregationField, Integer geoAggegationPrecision,
			Boolean onlyFilteredAggregation, MapBounds bounds) throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		BoolQueryBuilder masterBoolQuery = QueryBuilders.boolQuery();
		BoolQueryBuilder boolQuery = null;

		if (searchQuery != null && searchQuery.getAndBoolQueries() != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapBoolQuery query : searchQuery.getAndBoolQueries()) {
				if (query.getValues() != null)
					boolQuery.must(QueryBuilders.termsQuery(query.getKey(), query.getValues()));
				else
					boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (searchQuery != null && searchQuery.getOrBoolQueries() != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapBoolQuery query : searchQuery.getOrBoolQueries()) {
				if (query.getValues() != null)
					boolQuery.should(QueryBuilders.termsQuery(query.getKey(), query.getValues()));
				else
					boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (searchQuery != null && searchQuery.getAndRangeQueries() != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapRangeQuery query : searchQuery.getAndRangeQueries()) {
				boolQuery.must(QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd()));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (searchQuery != null && searchQuery.getOrRangeQueries() != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapRangeQuery query : searchQuery.getOrRangeQueries()) {
				boolQuery.should(QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd()));
			}
			masterBoolQuery.must(boolQuery);
		}

		if (searchQuery != null && searchQuery.getAndExistQueries() != null) {
			boolQuery = QueryBuilders.boolQuery();
			for (MapExistQuery query : searchQuery.getAndExistQueries()) {
				if (query.isExists())
					boolQuery.must(QueryBuilders.existsQuery(query.getKey()));
				else
					boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
			}
			masterBoolQuery.must(boolQuery);
		}

		MapResponse mapResponse = null;
		if(!onlyFilteredAggregation)
			mapResponse = querySearch(index, type, masterBoolQuery, from, limit, sortOn, sortType,
				geoAggregationField, geoAggegationPrecision);

		if (bounds != null) {
			GeoBoundingBoxQueryBuilder setCorners = QueryBuilders.geoBoundingBoxQuery(geoAggregationField)
					.setCorners(bounds.getTop(), bounds.getLeft(), bounds.getBottom(), bounds.getRight());
			masterBoolQuery.filter(setCorners);
			MapResponse filteredMapResponse = querySearch(index, type, masterBoolQuery, from, limit, sortOn, sortType,
					geoAggregationField, geoAggegationPrecision);
			
			mapResponse = onlyFilteredAggregation ? filteredMapResponse : mapResponse;
			mapResponse.setViewFilteredGeohashAggregation(filteredMapResponse.getGeohashAggregation());
		}

		return mapResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#geohashAggregation(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MapDocument geohashAggregation(String index, String type, String field, Integer precision)
			throws IOException {

		logger.info("GeoHash aggregation for index: {}, type: {} on field: {} with precision: {}", index, type, field,
				precision);

		return aggregateSearch(index, type, getGeoGridAggregationBuilder(field, precision));
	}

	private MapDocument aggregateSearch(String index, String type, AggregationBuilder aggQuery) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.aggregation(aggQuery);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
		logger.info("Aggregation search: {} completed", aggregation.getName());

		return new MapDocument(XContentHelper.toString(aggregation));

	}

	private GeoGridAggregationBuilder getGeoGridAggregationBuilder(String field, Integer precision) {
		GeoGridAggregationBuilder geohashGrid = AggregationBuilders
				.geohashGrid(field + "-" + String.valueOf(precision));
		geohashGrid.field(field);
		geohashGrid.precision(precision);
		return geohashGrid;
	}

}
