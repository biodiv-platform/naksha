package com.strandls.naksha.controllers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import com.strandls.naksha.common.ApiConstants;
import com.strandls.naksha.geoserver.GeoServerIntegrationService;
import com.strandls.naksha.geoserver.GeoserverService;
import com.strandls.naksha.geoserver.models.GeoserverLayerStyles;
import com.strandls.naksha.utils.Utils;

/**
 * Controller for geoserver related queries
 *
 * @author mukund
 *
 */
@Path(ApiConstants.GEOSERVER)
public class GeoserverController {

	@Inject
	GeoServerIntegrationService service;

	@GET
	@Path("/layers/{workspace}/wfs")
	@Produces(MediaType.APPLICATION_XML)
	public Document fetchAllLayers(@PathParam("workspace") String workspace) {

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("REQUEST", "GetCapabilities"));

		String url = workspace + "/wfs";
		String layers = new String(service.getRequest(url, params));
		return Utils.convertStringToDocument(layers);
	}

	@GET
	@Path("/layers/{workspace}/wms")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCapabilities(@PathParam("workspace") String workspace) {

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("REQUEST", "GetCapabilities"));

		String url = workspace + "/wms";
		String layers = new String(service.getRequest(url, params));
		return GeoserverService.jsonizeLayerString(layers);
	}

	@GET
	@Path("/layers/{id}/styles")
	@Produces(MediaType.APPLICATION_JSON)
	public List<GeoserverLayerStyles> fetchAllStyles(@PathParam("id") String id) {
		String url = "wms";

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("request", "GetStyles"));
		params.add(new BasicNameValuePair("layers", id));
		params.add(new BasicNameValuePair("service", "wms"));
		params.add(new BasicNameValuePair("version", "1.1.1"));

		String styleString = new String(service.getRequest(url, params));
		Document styleDocument = Utils.convertStringToDocument(styleString);
		return GeoserverService.getLayerStyles(styleDocument);
	}

	@GET
	@Path("/styles/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String fetchStyle(@PathParam("id") String id) {
		String url = "styles/" + id;
		return new String(service.getRequest(url, null));
	}

	@GET
	@Path("/thumbnails/{workspace}/{id}")
	@Produces("image/gif")
	public Response fetchThumbnail(@PathParam("id") String id, @PathParam("workspace") String wspace,
		@QueryParam("bbox") String para, @QueryParam("width") String width, @QueryParam("height") String height,
		@QueryParam("srs") String srs) {

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("request", "GetMap"));
		params.add(new BasicNameValuePair("layers", id));
		params.add(new BasicNameValuePair("service", "WMS"));
		params.add(new BasicNameValuePair("version", "1.1.0"));
		params.add(new BasicNameValuePair("bbox", para));
		params.add(new BasicNameValuePair("width", width));
		params.add(new BasicNameValuePair("height", height));
		params.add(new BasicNameValuePair("srs", srs));
		params.add(new BasicNameValuePair("format", "image/gif"));

		byte[] file = service.getRequest(wspace + "/wms", params);

		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@GET
	@Path("/legend/{layer}/{style}")
	@Produces("image/png")
	public Response fetchLegend(@PathParam("layer") String layer, @PathParam("style") String style) {

		String url = "wms";

		ArrayList<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("REQUEST", "GetLegendGraphic"));
		params.add(new BasicNameValuePair("VERSION", "1.0.0"));
		params.add(new BasicNameValuePair("FORMAT", "image/png"));
		params.add(new BasicNameValuePair("transparent", "true"));
		params.add(new BasicNameValuePair("LAYER", layer));
		params.add(new BasicNameValuePair("style", style));

		byte[] file = service.getRequest(url, params);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@GET
	@Path("/gwc/service/tms/1.0.0/{layer}/{z}/{x}/{y}")
	@Produces("application/x-protobuf")
	public Response fetchTiles(@PathParam("layer") String layer, @PathParam("z") String z, @PathParam("x") String x,
			@PathParam("y") String y) {

		String url = "gwc/service/tms/1.0.0/" + layer + "@EPSG%3A900913@pbf/" + z + "/" + x + "/" + y + ".pbf";

		byte[] file = service.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}
}
