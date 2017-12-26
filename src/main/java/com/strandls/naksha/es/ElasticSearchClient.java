package com.strandls.naksha.es;

import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * A wrapper around rest high level client to expose both high level and low
 * level client
 * 
 * @author mukund
 */
public class ElasticSearchClient extends RestHighLevelClient {

	public ElasticSearchClient(RestClientBuilder restClientBuilder) {
		super(restClientBuilder);
	}

}
