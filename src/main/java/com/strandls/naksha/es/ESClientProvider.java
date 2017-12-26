package com.strandls.naksha.es;

import javax.inject.Singleton;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.strandls.naksha.NakshaConfig;

/**
 * Provides an elastic search client
 * 
 * @author mukund
 *
 */
@Singleton
public class ESClientProvider {

	private static ElasticSearchClient client;

	private ESClientProvider() {
	}

	private static synchronized void initClient() {
		client = new ElasticSearchClient(RestClient.builder(HttpHost.create(NakshaConfig.getString("es.url"))));
	}

	public static ElasticSearchClient getClient() {

		if (client == null)
			initClient();

		return client;
	}

	public static RestClient getLowLevelClient() {
		if (client == null)
			initClient();

		return client.getLowLevelClient();
	}

}
