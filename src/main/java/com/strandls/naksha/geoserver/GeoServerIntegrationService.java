package com.strandls.naksha.geoserver;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes http get requests to geoserver
 * @author mukund
 *
 */
public class GeoServerIntegrationService {

	private final Logger logger = LoggerFactory.getLogger(GeoServerIntegrationService.class);

	/**
	 * The context helps maintain a session across http requests.
	 */
	private HttpClientContext context;

	/**
	 * The connection manager for this session. Maintains a pool of connections
	 * for the session.
	 */
	private PoolingHttpClientConnectionManager manager;

	/**
	 * The maximum number of connections to maintain per route by the pooling
	 * client manager
	 */
	private final int MAX_CONNECTIONS_PER_ROUTE = 5;

	public GeoServerIntegrationService() {
		initHttpConnection();
	}

	private void initHttpConnection() {
		manager = new PoolingHttpClientConnectionManager();
		manager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);

		CookieStore cookieStore = new BasicCookieStore();
		context = HttpClientContext.create();
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

	}

	/**
	 * Post request to given url
	 * 
	 * @param uri
	 * @param data
	 */

	public HttpEntity getRequest(String uri, List<NameValuePair> params) {
		
		CloseableHttpResponse response = null;
		
		try {
			
			URIBuilder builder = new URIBuilder(uri);
			builder.setParameters(params);
			HttpGet request = new HttpGet(builder.build());
			
			CloseableHttpClient httpclient = HttpClients.createDefault();

			try {
				response = httpclient.execute(request, context);
				return response.getEntity();
				
			} catch (IOException e) {
				logger.error("Error while trying to send request at URL {}", uri);
			} finally {
				if (response != null)
					HttpClientUtils.closeQuietly(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while trying to send request at URL {}", uri);
		}

		return null;
	}

}
