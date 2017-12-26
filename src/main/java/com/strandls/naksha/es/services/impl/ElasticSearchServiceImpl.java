package com.strandls.naksha.es.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.es.ESClientProvider;
import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.models.MapBoolQuery;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryStatus;
import com.strandls.naksha.es.models.MapRangeQuery;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSearchQuery;
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
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapResponse create(String index, String type, String documentId, String document) throws IOException {

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

		return new MapResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#fetch(java.lang.String, java.lang.String, java.lang.String)
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
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#update(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapResponse update(String index, String type, String documentId, String document) throws IOException {

		logger.info("Trying to update index: {}, type: {} & id: {}", index, type, documentId);

		UpdateRequest request = new UpdateRequest(index, type, documentId);
		request.doc(document, XContentType.JSON);
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

		return new MapResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#delete(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapResponse delete(String index, String type, String documentId) throws IOException {

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

		return new MapResponse(queryStatus, failureReason);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#bulkUpload(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<MapResponse> bulkUpload(String index, String type, String jsonArray) throws IOException {
		List<MapResponse> responses = new ArrayList<>();

		logger.info("Trying to bulk upload index: {}, type: {}", index, type);

		ObjectMapper mapper = new ObjectMapper();

		JsonNode[] jsons = null;
		try {
			jsons = mapper.readValue(jsonArray, JsonNode[].class);
		} catch (JsonParseException e) {
			logger.error("Json parsing exception while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapResponse(MapQueryStatus.JSON_EXCEPTION, "Json Parsing Exception"));
		} catch (JsonMappingException e) {
			logger.error("Json mapping exception while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapResponse(MapQueryStatus.JSON_EXCEPTION, "Json Mapping Exception"));
		}

		if (!responses.isEmpty())
			return responses;

		if (jsons != null && !jsons[0].has("id")) {
			logger.error("No id field specified while trying to bulk upload for index:{}, type: {}", index, type);
			responses.add(new MapResponse(MapQueryStatus.NO_ID, "No id field specified"));
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

			responses.add(new MapResponse(queryStatus, failureReason));
		}

		return responses;
	}

	private List<MapDocument> querySearch(String index, String type, QueryBuilder query, Integer from, Integer limit)
			throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		if (from != null)
			sourceBuilder.from(from);
		if (limit != null)
			sourceBuilder.size(limit);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits();
		if (totalHits == 0)
			return result;

		for (SearchHit hit : searchResponse.getHits().getHits())
			result.add(new MapDocument(hit.getSourceAsString()));

		return result;

	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#termSearch(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<MapDocument> termSearch(String index, String type, String key, String value, Integer from,
			Integer limit) throws IOException {

		logger.info("Term search for index: {}, type: {}, key: {}, value: {}", index, type, key, value);
		TermQueryBuilder query = QueryBuilders.termQuery(key, value);

		return querySearch(index, type, query, from, limit);

	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#boolSearch(java.lang.String, java.lang.String, java.util.List, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<MapDocument> boolSearch(String index, String type, List<MapBoolQuery> queries, Integer from,
			Integer limit) throws IOException {

		logger.info("Bool search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapBoolQuery query : queries) {
			boolQuery.must(QueryBuilders.termsQuery(query.key, query.values));
		}

		return querySearch(index, type, boolQuery, from, limit);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#rangeSearch(java.lang.String, java.lang.String, java.util.List, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<MapDocument> rangeSearch(String index, String type, List<MapRangeQuery> queries, Integer from,
			Integer limit) throws IOException {

		logger.info("Range search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapRangeQuery query : queries) {
			boolQuery.must(QueryBuilders.rangeQuery(query.key).from(query.start).to(query.end));
		}

		return querySearch(index, type, boolQuery, from, limit);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#search(java.lang.String, java.lang.String, com.strandls.naksha.es.models.MapSearchQuery, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<MapDocument> search(String index, String type, MapSearchQuery searchQery, Integer from, Integer limit)
			throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (MapBoolQuery query : searchQery.boolQueries) {
			boolQuery.must(QueryBuilders.termsQuery(query.key, query.values));
		}
		for (MapRangeQuery query : searchQery.rangeQueries) {
			boolQuery.must(QueryBuilders.rangeQuery(query.key).from(query.start).to(query.end));
		}

		return querySearch(index, type, boolQuery, from, limit);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticSearchService#geohashAggregation(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public MapDocument geohashAggregation(String index, String type, String field, Integer precision)
			throws IOException {

		logger.info("GeoHash aggregation for index: {}, type: {} on field: {} with precision: {}", index, type, field,
				precision);

		GeoGridAggregationBuilder geohashGrid = AggregationBuilders
				.geohashGrid(field + "-" + String.valueOf(precision));
		geohashGrid.field(field);
		geohashGrid.precision(precision);

		return aggregateSearch(index, type, geohashGrid);
	}

	private MapDocument aggregateSearch(String index, String type, AggregationBuilder aggQuery) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.aggregation(aggQuery);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		Aggregation aggregation = searchResponse.getAggregations().asList().get(0);

		return new MapDocument(XContentHelper.toString(aggregation));

	}

}
