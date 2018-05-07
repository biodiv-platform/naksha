package com.strandls.naksha;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author mukund
 *
 */
@Provider
public class NakshaResponseFilter implements ContainerResponseFilter {

	public static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD";
	public static final int MAX_AGE = 42 * 60 * 60;
	public static final String DEFAULT_ALLOWED_HEADERS = "origin, content-type, accept, authorization, X-Requested-With, X-Auth-Token, X-AppKey";

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add("X-Powered-By", "Naksha Webapp");

		String origin = requestContext.getHeaderString("Origin");
		if (!isValidOrigin(origin)) {
			origin = "*";
		}
		headers.add("Access-Control-Allow-Origin", origin);

		headers.add("Access-Control-Allow-Headers", getRequestedAllowedHeaders(requestContext));
		headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
	}

	private String getRequestedAllowedHeaders(ContainerRequestContext responseContext) {
		List<String> headers = responseContext.getHeaders().get("Access-Control-Allow-Headers");
		return createHeaderList(headers, DEFAULT_ALLOWED_HEADERS);
	}

	private String createHeaderList(List<String> headers, String defaultHeaders) {
		if (headers == null || headers.isEmpty()) {
			return defaultHeaders;
		}
		StringBuilder retVal = new StringBuilder();
		for (int i = 0; i < headers.size(); i++) {
			String header = headers.get(i);
			retVal.append(header);
			retVal.append(',');
		}
		retVal.append(defaultHeaders);
		return retVal.toString();
	}

	private boolean isValidOrigin(String origin) {
		return StringUtils.contains(origin, NakshaConfig.getInt("allow.origin")) ? true : false;
	}

}
