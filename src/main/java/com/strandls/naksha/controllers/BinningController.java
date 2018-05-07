package com.strandls.naksha.controllers;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.strandls.naksha.binning.models.GeojsonData;
import com.strandls.naksha.binning.services.BinningService;
import com.strandls.naksha.es.models.MapBounds;

/**
 * Controller for binning related services
 * @author mukund
 *
 */
@Path("binning")
public class BinningController {

	BinningService service = new BinningService();
	
	@GET
	@Path("/square/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public GeojsonData bin(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("geoField") String geoField,
			@QueryParam("cellSideKm") Double cellSideKm,
			MapBounds bounds) {

		
		try {
			return service.squareBin(index, type, geoField, bounds, cellSideKm);
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
