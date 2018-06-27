package com.strandls.naksha.controllers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.strandls.naksha.layers.models.LayerAttributes;
import com.strandls.naksha.layers.services.LayerService;

/**
 * Controller of layers in db
 * 
 * @author mukund
 *
 */
@Path("layer")
public class LayerController {

	@Inject
	LayerService layerService;

	@Path("/uploadshp")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public int uploadShp(FormDataMultiPart multiPart) {

		try {

			FormDataBodyPart formdata = multiPart.getField("shp");

			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Shp file not present").build());
			}
			InputStream shpInputStream = formdata.getValueAs(InputStream.class);
			String shpFileName = formdata.getContentDisposition().getFileName();
			shpFileName += ".";
			String layerName = shpFileName.split("\\.")[0].toLowerCase();

			formdata = multiPart.getField("dbf");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Dbf file not present").build());
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

			return layerService.uploadShpLayer(shpInputStream, dbfInputStream, metadataInputStream, shxInputStream,
					layerName);
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@Path("/uploadraster")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public int uploadRaster(FormDataMultiPart multiPart) {

		try {
			FormDataBodyPart formdata = null;

			Map<String, InputStream> rasterFileData = new HashMap<>();
			int i = 1;
			while(true) {
				formdata = multiPart.getField("raster" + i);

				if(formdata == null) break;

				String rasterFileName = formdata.getContentDisposition().getFileName();
				InputStream rasterInputStream = formdata.getValueAs(InputStream.class);
				rasterFileData.put(rasterFileName, rasterInputStream);

				i++;
			}

			if(i == 0) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Raster file not present").build());
			}

			formdata = multiPart.getField("metadata");
			if (formdata == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.BAD_REQUEST).entity("Metadata file not present").build());
			}
			InputStream metadataInputStream = formdata.getValueAs(InputStream.class);

			return layerService.uploadRasterLayer(rasterFileData, metadataInputStream);
		} catch (Exception e) {
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path("/attributes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LayerAttributes> attributes(@QueryParam("layername") String layername) {
		return layerService.getLayerAttributes(layername);
	}

	@GET
	@Path("/tags")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> tags(@QueryParam("tag") String tag) {
		return layerService.getLayerNamesWithTag(tag);
	}

}
