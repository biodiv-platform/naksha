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
			@QueryParam("east") Double east,
			@QueryParam("west") Double west,
			@QueryParam("north") Double north,
			@QueryParam("south") Double south,
			@QueryParam("cellSideKm") Double cellSideKm) {

		
		try {
			return service.squareBin(index, type, geoField, east, west, north, south, cellSideKm);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
