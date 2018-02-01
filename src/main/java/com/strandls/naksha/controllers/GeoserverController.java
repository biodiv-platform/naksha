package com.strandls.naksha.controllers;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import com.strandls.naksha.geoserver.GeoServerIntegrationService;
import com.strandls.naksha.utils.Utils;

/**
 * Controller for geoserver related queries
 * 
 * @author mukund
 *
 */
@Path("geoserver")
public class GeoserverController {

	GeoServerIntegrationService service = new GeoServerIntegrationService();

	@GET
	@Path("/layers/{workspace}/wfs")
	@Produces(MediaType.APPLICATION_XML)
	public Document fetchAllLayers(@PathParam("workspace") String workspace) {

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("REQUEST", "GetCapabilities"));

		String url = workspace + "/wfs";
		String layers = service.getRequest(url, params);
		return Utils.convertStringToDocument(layers);
	}

	@GET
	@Path("/layers/{id}/styles")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchAllStyles(@PathParam("id") String id) {
		String url = "rest/layers/" + id + "/styles.json";
		return service.getRequest(url, null);
	}

	@GET
	@Path("/styles/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchStyle(@PathParam("id") String id) {
		String url = "styles/" + id;
		return service.getRequest(url, null);
	}

}
