package com.strandls.naksha.controllers;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.services.api.ElasticSearchGeoService;

/**
 * Controller for geo related query services
 * @author mukund
 *
 */
@Path("geo")
public class GeoController {

	@Inject
	ElasticSearchGeoService service;
	
	@GET
	@Path("/within/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse within(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("geoField") String geoField,
			@QueryParam("top") double top,
			@QueryParam("left") double left,
			@QueryParam("bottom") double bottom,
			@QueryParam("right") double right) {
		
		try {
			return service.getGeoWithinDocuments(index, type, geoField, top, left, bottom, right);
		} catch (IOException e) {
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
}
