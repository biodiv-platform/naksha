package com.strandls.naksha.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Controller for geoserver related queries
 * @author mukund
 *
 */
@Path("geoserver")
public class GeoserverController {
	
	@GET
	@Path("/layers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> layers() {
		return new ArrayList<>(); 
	}

	@GET
	@Path("/layers/{id}/styles")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> styles(@PathParam("id") String id) {
		return new ArrayList<>();
	}

	@GET
	@Path("/layers/{id}/styles/{styleId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> style(@PathParam("id") String id,
			@PathParam("styleId") String styleId) {
		return new ArrayList<>();
	}
}
