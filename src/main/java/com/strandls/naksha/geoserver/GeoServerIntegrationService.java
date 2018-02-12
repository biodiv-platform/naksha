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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;

/**
 * Makes http get requests to geoserver
 * 
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
	 * The connection manager for this session. Maintains a pool of connections for
	 * the session.
	 */
	private PoolingHttpClientConnectionManager manager;

	/**
	 * The maximum number of connections to maintain per route by the pooling client
	 * manager
	 */
	private final int MAX_CONNECTIONS_PER_ROUTE = 5;

	/**
	 * Base url of geoserver
	 */
	private final String BASE_URL = NakshaConfig.getString("geoserver.url");

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
	 * Makes http get request to geoserver
	 * @param uri the uri to hit
	 * @param params the parameters with the url
	 * @return byte[] response
	 */
	public byte[] getRequest(String uri, List<NameValuePair> params) {

		CloseableHttpResponse response = null;
		byte[] byteArrayResponse = null;

		try {

			URIBuilder builder = new URIBuilder(BASE_URL + uri);
			if (params != null)
				builder.setParameters(params);
			HttpGet request = new HttpGet(builder.build());

			CloseableHttpClient httpclient = HttpClients.createDefault();

			try {
				response = httpclient.execute(request, context);
				HttpEntity entity = response.getEntity();
				byteArrayResponse = EntityUtils.toByteArray(entity);
				EntityUtils.consume(entity);

			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Error while trying to send request at URL {}", uri);
			} finally {
				if (byteArrayResponse != null)
					HttpClientUtils.closeQuietly(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while trying to send request at URL {}", uri);
		}

		return byteArrayResponse;
	}
}
