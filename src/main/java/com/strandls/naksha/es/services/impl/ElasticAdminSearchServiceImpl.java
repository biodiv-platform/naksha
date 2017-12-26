package com.strandls.naksha.es.services.impl;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.es.ESClientProvider;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryStatus;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.services.api.ElasticAdminSearchService;

/**
 * Implementation of {@link ElasticAdminSearchService}
 * 
 * @author mukund
 *
 */
public class ElasticAdminSearchServiceImpl implements ElasticAdminSearchService {

	private final RestClient client = ESClientProvider.getLowLevelClient();

	private final Logger logger = LoggerFactory.getLogger(ElasticAdminSearchServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#postMapping(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public MapResponse postMapping(String index, String type, String mapping) throws IOException {

		logger.info("Trying to add mapping to index: {}", index);

		StringEntity entity = null;
		if (!Strings.isNullOrEmpty(mapping)) {
			entity = new StringEntity(mapping, ContentType.APPLICATION_JSON);
		}

		Response response = client.performRequest("PUT", index + "/" + type + "/_mapping", new HashMap<>(), entity);
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Added mapping to index: {} with status: {}", index, status);

		return new MapResponse(MapQueryStatus.UNKNOWN, status);
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#getMapping(java.lang.String)
	 */
	@Override
	public MapDocument getMapping(String index) throws IOException {

		logger.info("Trying to get mapping for index: {}", index);

		Response response = client.performRequest("GET", index + "/_mapping");
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Retrieved mapping for index: {} with status: {}", index, status);

		return new MapDocument(EntityUtils.toString(response.getEntity()));
	}

	/*
	 * (non-Javadoc)
	 * @see com.strandls.naksha.es.services.api.ElasticAdminSearchService#createIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public MapResponse createIndex(String index, String type) throws IOException {

		logger.info("Trying to create index: {}", index);

		Response response = client.performRequest("PUT", "/" + index);
		String status = response.getStatusLine().getReasonPhrase();

		logger.info("Created index: {} with status: {}", index, status);

		return new MapResponse(MapQueryStatus.UNKNOWN, status);
	}
}
