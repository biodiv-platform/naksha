package com.strandls.naksha.es.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.services.api.ElasticSearchGeoService;

/**
 * Implementation of {@link ElasticSearchGeoService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchGeoServiceImpl implements ElasticSearchGeoService {

	private final Logger logger = LoggerFactory.getLogger(ElasticSearchGeoServiceImpl.class);
	
	@Inject
	private ElasticSearchClient client;

	private MapResponse querySearch(String index, String type, QueryBuilder query) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.query(query);
		sourceBuilder.from(0);
		sourceBuilder.size(500);
		
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);
		List<MapDocument> result = new ArrayList<>();

		long totalHits = searchResponse.getHits().getTotalHits();
	
		for (SearchHit hit : searchResponse.getHits().getHits())
			result.add(new MapDocument(hit.getSourceAsString()));

		logger.info("Search completed with total hits: {}", totalHits);
		
		return new MapResponse(result, totalHits, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.strandls.naksha.es.services.api.ElasticSearchGeoService#
	 * getGeoWithinDocuments(java.lang.String, java.lang.String, java.lang.String,
	 * double, double, double, double)
	 */
	@Override
	public MapResponse getGeoWithinDocuments(String index, String type, String geoField, double top, double left,
			double bottom, double right) throws IOException {

		logger.info("Geo with search, top: {}, left: {}, bottom: {}, right: {}", top, left, bottom, right);
		GeoBoundingBoxQueryBuilder query = QueryBuilders.geoBoundingBoxQuery(geoField).setCorners(top, left, bottom,
				right);

		return querySearch(index, type, query);
	}

}
