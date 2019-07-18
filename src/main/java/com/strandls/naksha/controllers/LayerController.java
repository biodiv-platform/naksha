package com.strandls.naksha.controllers;

import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.naksha.Upload.layers.LayerUploadService;
import com.strandls.naksha.common.ApiConstants;
import com.strandls.naksha.geoserver.GeoServerIntegrationService;

/**
 * 
 * 
 */
@Path(ApiConstants.LAYERS)
public class LayerController {

	@Inject
	LayerUploadService layerService;
	GeoServerIntegrationService service;

	@POST
	@Path(ApiConstants.UPLOADSHP)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFiles(final FormDataMultiPart multiPart) {

		try {
			FormDataBodyPart formdata = multiPart.getField("shp");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("SHP file not present").build());

			}

			InputStream shpInputStream = formdata.getValueAs(InputStream.class);
			String shpFileName = formdata.getContentDisposition().getFileName();
			shpFileName += ".";
			String layerName = shpFileName.split("\\.")[0].toLowerCase();
			System.out.println(layerName);

			formdata = multiPart.getField("dbf");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("DBF file not present").build());
			}
			InputStream dbfInputStream = formdata.getValueAs(InputStream.class);

			formdata = multiPart.getField("metadata");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
			}
			InputStream metadataInputStream = formdata.getValueAs(InputStream.class);

			formdata = multiPart.getField("shx");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Shx file not present").build());
			}
			InputStream shxInputStream = formdata.getValueAs(InputStream.class);

			int i = layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream,
					layerName);
			
			// Waiting for disk files to be created then reload layers 
			Thread.sleep(5000);
			service.getRequest("/rest/reload", null, "POST");

			return Response.status(Response.Status.OK).entity("{\"responseCode\":"+i+", \"info\": \"1 = failure && 0 = Success\"}").build();

		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());

		}

	}
//	@GET
//	@Path("/attributes")
//	@Produces(MediaType.APPLICATION_JSON)
//	public List<LayerAttributes> attributes(@QueryParam("layername") String layername) {
//		return layerService.getLayerAttributes(layername);
//	}
//	
//	@GET
//	@Path("/tags")
//	@Produces(MediaType.APPLICATION_JSON)
//	public List<String> tags(@QueryParam("tag") String tag) {
//		return layerService.getLayerNamesWithTag(tag);
//	}

}