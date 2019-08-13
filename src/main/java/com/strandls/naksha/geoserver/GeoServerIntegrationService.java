package com.strandls.naksha.geoserver;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

	private HttpClientContext context;

	private final String baseUrl = NakshaConfig.getString("geoserver.url");
	private final String gsWebUsername = NakshaConfig.getString("geoserver.web.username");
	private final String gsWebPassword = NakshaConfig.getString("geoserver.web.password");

	@Inject
	public GeoServerIntegrationService() {
		initHttpConnection();
	}

	private void initHttpConnection() {
		CookieStore cookieStore = new BasicCookieStore();
		context = HttpClientContext.create();
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(gsWebUsername, gsWebPassword));
		context.setCredentialsProvider(credentialsProvider);
	}

	public byte[] getRequest(String uri, List<NameValuePair> params) {
		return this.getRequest(uri, params, "GET");
	}

	/**
	 * Makes http get request to geoserver
	 * 
	 * @param uri    the uri to hit
	 * @param params the parameters with the url
	 * @return byte[] response
	 */
	public byte[] getRequest(String uri, List<NameValuePair> params, String method) {

		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = null;
		byte[] byteArrayResponse = null;

		try {
			URIBuilder builder = new URIBuilder(baseUrl + uri);
			httpclient = HttpClients.createDefault();
			if (params != null)
				builder.setParameters(params);
			if (method == "POST") {
				HttpPost request = new HttpPost(builder.build());
				response = httpclient.execute(request, context);
			} else {
				HttpGet request = new HttpGet(builder.build());
				response = httpclient.execute(request, context);
			}
			HttpEntity entity = response.getEntity();
			byteArrayResponse = EntityUtils.toByteArray(entity);
			EntityUtils.consume(entity);

		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("Error while trying to send request at URL {}", uri);
		} finally {
			if (byteArrayResponse != null)
				HttpClientUtils.closeQuietly(response);
			try {
				if (httpclient != null)
					httpclient.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}

		return byteArrayResponse != null ? byteArrayResponse : new byte[0];
	}
}
