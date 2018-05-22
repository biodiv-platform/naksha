package com.strandls.naksha.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.w3c.dom.Document;

import com.strandls.naksha.geoserver.GeoServerIntegrationService;
import com.strandls.naksha.geoserver.GeoserverService;
import com.strandls.naksha.geoserver.LayerService;
import com.strandls.naksha.geoserver.models.GeoserverLayerStyles;
import com.strandls.naksha.utils.Utils;

/**
 * Controller for geoserver related queries
 *
 * @author mukund
 *
 */
@Path("geoserver")
public class GeoserverController {

	@Inject
	GeoServerIntegrationService service;

	@Inject
	LayerService layerService;

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
	@Path("/thumbnails/{id}")
	@Produces("image/gif")
	public Response fetchThumbnail(@PathParam("id") String id) {

		String url = "www/map_thumbnails/" + id;
		byte[] file = service.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@GET
	@Path("/legend/{layer}/{style}")
	@Produces("image/png")
	public Response fetchLegend(@PathParam("layer") String layer,
			@PathParam("style") String style) {

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
	public Response fetchTiles(@PathParam("layer") String layer,
			@PathParam("z") String z,
			@PathParam("x") String x,
			@PathParam("y") String y) {

		String url = "gwc/service/tms/1.0.0/" + layer + "@EPSG%3A900913@pbf/" + z + "/" + x + "/" + y + ".pbf";

		byte[] file = service.getRequest(url, null);
		return Response.ok(new ByteArrayInputStream(file)).build();
	}

	@Path("/uploadshp")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public int uploadShp(FormDataMultiPart multiPart) {

        try {

            FormDataBodyPart formdata = multiPart.getField("shp");

            if (formdata == null) {
            	throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Shp file not present").build());
            }
            InputStream shpInputStream = formdata.getValueAs(InputStream.class);
            String shpFileName = formdata.getContentDisposition().getFileName();
            shpFileName += ".";
            String layerName = shpFileName.split("\\.")[0].toLowerCase();

            formdata = multiPart.getField("dbf");
            if (formdata == null) {
            	throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Dbf file not present").build());
            }
            InputStream dbfInputStream = formdata.getValueAs(InputStream.class);

            formdata = multiPart.getField("metadata");
            if (formdata == null) {
            	throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Metadata file not present").build());
            }
            InputStream metadataInputStream = formdata.getValueAs(InputStream.class);

			formdata = multiPart.getField("shx");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Shx file not present").build());
			}
			InputStream shxInputStream = formdata.getValueAs(InputStream.class);

            return layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream, layerName);
        } catch (Exception e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
        }
    }
}
