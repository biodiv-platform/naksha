package com.strandls.naksha.es.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.missing.Missing;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.models.AggregationResponse;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryResponse;
import com.strandls.naksha.es.models.MapQueryStatus;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSearchParams;
import com.strandls.naksha.es.models.MapSortType;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;
import com.strandls.naksha.es.services.api.ElasticSearchService;

/**
 * Implementation of {@link ElasticSearchService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchServiceImpl extends ElasticSearchQueryUtil implements ElasticSearchService {

	@Inject
	private ElasticSearchClient client;

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

		StringBuilder failureReason = new StringBuilder();

		if (shardInfo.getFailed() > 0) {

			for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
				failureReason.append(failure.reason());
				failureReason.append(";");
			}
		}

		MapQueryStatus queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());

		logger.info("Created index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

		return new MapQueryResponse(queryStatus, failureReason.toString());
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

		logger.info("Updated index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

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

		logger.info("Deleted index: {}, type: {} & id: {} with status {}", index, type, documentId, queryStatus);

		return new MapQueryResponse(queryStatus, failureReason);
	}

	private JsonNode[] parseJson(String jsonArray, List<MapQueryResponse> responses) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		JsonNode[] jsons = null;
		try {
			jsons = mapper.readValue(jsonArray, JsonNode[].class);
		} catch (JsonParseException e) {
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Parsing Exception"));
		} catch (JsonMappingException e) {
			responses.add(new MapQueryResponse(MapQueryStatus.JSON_EXCEPTION, "Json Mapping Exception"));
		}

		if (jsons != null && !jsons[0].has("id")) {
			responses.add(new MapQueryResponse(MapQueryStatus.NO_ID, "No id field specified"));
		}

		return jsons;
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

		JsonNode[] jsons = parseJson(jsonArray, responses);
		if (!responses.isEmpty()) {
			logger.error("Json exception-{}, while trying to bulk upload for index:{}, type: {}",
					responses.get(0).getMessage(), index, type);
			return responses;
		}

		BulkRequest request = new BulkRequest();

		for (JsonNode json : jsons)
			request.add(
					new IndexRequest(index, type, json.get("id").asText()).source(json.toString(), XContentType.JSON));

		BulkResponse bulkResponse = client.bulk(request);

		for (BulkItemResponse bulkItemResponse : bulkResponse) {

			StringBuilder failureReason = new StringBuilder();
			MapQueryStatus queryStatus;

			if (bulkItemResponse.isFailed()) {
				failureReason.append(bulkItemResponse.getFailureMessage());
				queryStatus = MapQueryStatus.ERROR;
			} else {
				IndexResponse indexResponse = bulkItemResponse.getResponse();
				ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();

				if (shardInfo.getFailed() > 0) {

					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						failureReason.append(failure.reason());
						failureReason.append(";");
					}
				}

				queryStatus = MapQueryStatus.valueOf(indexResponse.getResult().name());
			}

			logger.info(" For index: {}, type: {}, bulk upload id: {}, the status is {}", index, type,
					bulkItemResponse.getId(), queryStatus);

			responses.add(new MapQueryResponse(queryStatus, failureReason.toString()));
		}

		return responses;
	}

	@Override
	public List<MapQueryResponse> bulkUpdate(String index, String type, List<Map<String, Object>> updateDocs)
			throws IOException {

		logger.info("Trying to bulk update index: {}, type: {}", index, type);

		BulkRequest request = new BulkRequest();

		for (Map<String, Object> doc : updateDocs)
			request.add(new UpdateRequest(index, type, doc.get("id").toString()).doc(doc));

		BulkResponse bulkResponse = client.bulk(request);

		List<MapQueryResponse> responses = new ArrayList<>();

		for (BulkItemResponse bulkItemResponse : bulkResponse) {

			StringBuilder failureReason = new StringBuilder();
			MapQueryStatus queryStatus;

			if (bulkItemResponse.isFailed()) {
				failureReason.append(bulkItemResponse.getFailureMessage());
				queryStatus = MapQueryStatus.ERROR;
			} else {
				UpdateResponse updateResponse = bulkItemResponse.getResponse();
				ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();

				if (shardInfo.getFailed() > 0) {

					for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
						failureReason.append(failure.reason());
						failureReason.append(";");
					}
				}

				queryStatus = MapQueryStatus.valueOf(updateResponse.getResult().name());
			}

			logger.info(" For index: {}, type: {}, bulk update id: {}, the status is {}", index, type,
					bulkItemResponse.getId(), queryStatus);

			responses.add(new MapQueryResponse(queryStatus, failureReason.toString()));
		}

		return responses;

	}

	private MapResponse querySearch(String index, String type, QueryBuilder query, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		if (searchParams.getFrom() != null)
			sourceBuilder.from(searchParams.getFrom());
		if (searchParams.getLimit() != null)
			sourceBuilder.size(searchParams.getLimit());

		if (searchParams.getSortOn() != null) {
			SortOrder sortOrder = searchParams.getSortType() != null && MapSortType.ASC == searchParams.getSortType()
					? SortOrder.ASC
					: SortOrder.DESC;
			sourceBuilder.sort(searchParams.getSortOn(), sortOrder);
		}

		if (geoAggregationField != null) {
			geoAggegationPrecision = geoAggegationPrecision != null ? geoAggegationPrecision : 1;
			sourceBuilder.aggregation(getGeoGridAggregationBuilder(geoAggregationField, geoAggegationPrecision));
		}

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);
		System.out.println(searchRequest);
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
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse termSearch(String index, String type, String key, String value, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Term search for index: {}, type: {}, key: {}, value: {}", index, type, key, value);
		QueryBuilder query;
		if (value != null)
			query = QueryBuilders.termQuery(key, value);
		else
			query = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(key));

		return querySearch(index, type, query, searchParams, geoAggregationField, geoAggegationPrecision);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#boolSearch(java.lang
	 * .String, java.lang.String, java.util.List,
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse boolSearch(String index, String type, List<MapBoolQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Bool search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapBoolQuery query : queries) {
			if (query.getValues() != null)
				boolQuery.must(QueryBuilders.termsQuery(query.getKey(), query.getValues()));
			else
				boolQuery.mustNot(QueryBuilders.existsQuery(query.getKey()));
		}

		return querySearch(index, type, boolQuery, searchParams, geoAggregationField, geoAggegationPrecision);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#rangeSearch(java.
	 * lang.String, java.lang.String, java.util.List,
	 * com.strandls.naksha.es.models.MapSearchParams, java.lang.String,
	 * java.lang.Integer)
	 */
	@Override
	public MapResponse rangeSearch(String index, String type, List<MapRangeQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException {

		logger.info("Range search for index: {}, type: {}", index, type);
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (MapRangeQuery query : queries) {
			boolQuery.must(QueryBuilders.rangeQuery(query.getKey()).from(query.getStart()).to(query.getEnd()));
		}

		return querySearch(index, type, boolQuery, searchParams, geoAggregationField, geoAggegationPrecision);
	}

	@Override
	public AggregationResponse aggregation(String index, String type, MapSearchQuery searchQuery,String geoAggregationField,String filter) throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		MapSearchParams searchParams = searchQuery.getSearchParams();
		BoolQueryBuilder masterBoolQuery = getBoolQueryBuilder(searchQuery);
		applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);
		
		AggregationBuilder aggregation = AggregationBuilders.terms(filter).field(filter).size(1000);
		AggregationResponse aggregationResponse = new AggregationResponse() ;

		if(filter.equals("name") || filter.equals("status")) {
			AggregationResponse temp=null;
			aggregation = AggregationBuilders.filter("available", QueryBuilders.existsQuery(filter));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery,filter);
			HashMap<Object, Long> t = new HashMap<Object, Long>();
			for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
				t.put(entry.getKey(), entry.getValue());
			}
			aggregation = AggregationBuilders.missing("miss").field(filter.concat(".keyword"));
			temp = groupAggregation(index, type, aggregation, masterBoolQuery,filter);
			for (Map.Entry<Object, Long> entry : temp.getGroupAggregation().entrySet()) {
				t.put(entry.getKey(), entry.getValue());
			}
			aggregationResponse.setGroupAggregation(t);
		}
		else {
			aggregationResponse = groupAggregation(index, type, aggregation, masterBoolQuery,filter);
			
		}
		return aggregationResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#search(java.lang.
	 * String, java.lang.String, com.strandls.naksha.es.models.query.MapSearchQuery,
	 * java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.String)
	 */
	@Override
	public MapResponse search(String index, String type, MapSearchQuery searchQuery, String geoAggregationField,
			Integer geoAggegationPrecision, Boolean onlyFilteredAggregation, String termsAggregationField)
			throws IOException {

		logger.info("SEARCH for index: {}, type: {}", index, type);

		MapSearchParams searchParams = searchQuery.getSearchParams();
		BoolQueryBuilder masterBoolQuery = getBoolQueryBuilder(searchQuery);

		GeoGridAggregationBuilder geoGridAggregationBuilder = getGeoGridAggregationBuilder(geoAggregationField,
				geoAggegationPrecision);
		MapDocument aggregateSearch = aggregateSearch(index, type, geoGridAggregationBuilder, masterBoolQuery);
		String geohashAggregation = null;
		if (aggregateSearch != null)
			geohashAggregation = aggregateSearch.getDocument().toString();

		String termsAggregation = null;
		if(termsAggregationField != null) {
			termsAggregation = termsAggregation(index, type, termsAggregationField, null, null, geoAggregationField, searchQuery).getDocument().toString();
		}

		if (onlyFilteredAggregation != null && onlyFilteredAggregation) {
			applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);
			aggregateSearch = aggregateSearch(index, type, geoGridAggregationBuilder, masterBoolQuery);
			if (aggregateSearch != null)
				geohashAggregation = aggregateSearch.getDocument().toString();
			return new MapResponse(new ArrayList<>(), 0, geohashAggregation, geohashAggregation, termsAggregation);
		}

		applyMapBounds(searchParams, masterBoolQuery, geoAggregationField);
		MapResponse mapResponse = querySearch(index, type, masterBoolQuery, searchParams, geoAggregationField,
				geoAggegationPrecision);
		mapResponse.setViewFilteredGeohashAggregation(mapResponse.getGeohashAggregation());
		mapResponse.setGeohashAggregation(geohashAggregation);
		mapResponse.setTermsAggregation(termsAggregation);

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

		return aggregateSearch(index, type, getGeoGridAggregationBuilder(field, precision), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.strandls.naksha.es.services.api.ElasticSearchService#termsAggregation(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.Integer, java.lang.String,
	 * com.strandls.naksha.es.models.query.MapSearchQuery)
	 */
	@Override
	public MapDocument termsAggregation(String index, String type, String field, String subField, Integer size,
			String locationField, MapSearchQuery query) throws IOException {

		if (size == null)
			size = 500;

		logger.info("Terms aggregation for index: {}, type: {} on field: {} and sub field: {} with size: {}", index,
				type, field, subField, size);

		BoolQueryBuilder boolQuery = getBoolQueryBuilder(query);
		if (query.getSearchParams() != null) {
			applyMapBounds(query.getSearchParams(), boolQuery, locationField);
		}

		return aggregateSearch(index, type, getTermsAggregationBuilder(field, subField, size), boolQuery);
	}

	private MapDocument aggregateSearch(String index, String type, AggregationBuilder aggQuery, QueryBuilder query)
			throws IOException {

		if (aggQuery == null)
			return null;

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		if (query != null)
			sourceBuilder.query(query);
		sourceBuilder.aggregation(aggQuery);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		Aggregation aggregation = searchResponse.getAggregations().asList().get(0);
		logger.info("Aggregation search: {} completed", aggregation.getName());

		return new MapDocument(XContentHelper.toString(aggregation));

	}

	private AggregationResponse groupAggregation(String index, String type, AggregationBuilder aggQuery,
			QueryBuilder query, String filter) throws IOException {

		if (aggQuery == null)
			return null;

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		if (query != null)
			sourceBuilder.query(query);
		sourceBuilder.aggregation(aggQuery);

		SearchRequest request = new SearchRequest(index);
		request.types(type);
		request.source(sourceBuilder);

		SearchResponse response = client.search(request);

		HashMap<Object, Long> groupMonth = new HashMap<Object, Long>();

		if (filter.equals("name") || filter.equals("status")) {
			Filter filterAgg = response.getAggregations().get("available");
			if(filterAgg != null) {
				groupMonth.put("available",  filterAgg.getDocCount());				
			}
			Missing missingAgg = response.getAggregations().get("miss");
			if(missingAgg!= null) {
				groupMonth.put("missing",missingAgg.getDocCount());
			}
			
		} else {
			Terms frommonth = response.getAggregations().get(filter);

			for (Terms.Bucket entry : frommonth.getBuckets()) {
				groupMonth.put(entry.getKey(), entry.getDocCount());
			}
		}
		
		return new AggregationResponse(groupMonth);
	}
}
