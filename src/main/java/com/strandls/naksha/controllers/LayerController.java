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

/**
 * 
 * 
 */
@Path(ApiConstants.LAYERS)
public class LayerController {

	@Inject
	LayerUploadService layerService;

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

			return Response.status(Response.Status.OK).entity("data is inserted").build();

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