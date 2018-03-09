package com.strandls.naksha.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.es.models.MapBounds;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryResponse;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSortType;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;
import com.strandls.naksha.es.services.api.ElasticAdminSearchService;
import com.strandls.naksha.es.services.api.ElasticSearchService;
import com.strandls.naksha.es.services.impl.ElasticAdminSearchServiceImpl;
import com.strandls.naksha.es.services.impl.ElasticSearchServiceImpl;

/**
 * 
 * @author mukund
 *
 */
@Path("services")
public class NakshaController {

	ElasticSearchService esService = new ElasticSearchServiceImpl();
	ElasticAdminSearchService esAdminService = new ElasticAdminSearchServiceImpl();
	
	@POST
	@Path("/data/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse create(@PathParam("index") String index,
			@PathParam("type") String type,
			@PathParam("documentId") String documentId,
			MapDocument document) {

		String docString = String.valueOf(document.getDocument());
		try {
			new ObjectMapper().readValue(docString, Map.class);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}
		
		try {
			return esService.create(index, type, documentId, docString);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@GET
	@Path("/data/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument fetch(@PathParam("index") String index,
			@PathParam("type") String type,
			@PathParam("documentId") String documentId) {
		
		try {
			return esService.fetch(index, type, documentId);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@PUT
	@Path("/data/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse update(@PathParam("index") String index,
			@PathParam("type") String type,
			@PathParam("documentId") String documentId,
			Map<String, Object> document) {
		
		try {
			return esService.update(index, type, documentId, document);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@DELETE
	@Path("/data/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse delete(@PathParam("index") String index,
			@PathParam("type") String type,
			@PathParam("documentId") String documentId) {
		
		try {
			return esService.delete(index, type, documentId);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
		
	}
	
	@POST
	@Path("/bulk-upload/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MapQueryResponse> bulkUpload(@PathParam("index") String index,
			@PathParam("type") String type,
			String jsonArray) {
		
		try {
			return esService.bulkUpload(index, type, jsonArray);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@GET
	@Path("/term-search/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse search(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("key") String key,
			@QueryParam("value") String value,
			@QueryParam("from") Integer from,
			@QueryParam("limit") Integer limit,
			@QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision) {
		
		if(key == null || value == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("key or value not specified").build());
		
		try {
			return esService.termSearch(index, type, key, value, from, limit,
					sortOn, sortType, geoAggregationField, geoAggegationPrecision);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Path("/terms-search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse boolSearch(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("from") Integer from,
			@QueryParam("limit") Integer limit,
			@QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			List<MapBoolQuery> query) {
		
		try {
			return esService.boolSearch(index, type, query, from, limit,
					sortOn, sortType, geoAggregationField, geoAggegationPrecision);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Path("/range-search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse rangeSearch(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("from") Integer from,
			@QueryParam("limit") Integer limit,
			@QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			List<MapRangeQuery> query) {
		
		try {
			return esService.rangeSearch(index, type, query, from, limit,
					sortOn, sortType, geoAggregationField, geoAggegationPrecision);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@GET
	@Path("/geohash-aggregation/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument geohashAggregation(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("geoAggregationField") String field,
			@QueryParam("geoAggegationPrecision") Integer precision) {
		
		if(field == null || precision == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("Field or precision not specified").build());
		if(precision < 1 || precision > 12)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("Precision value must be between 1 and 12").build());
		
		try {
			return esService.geohashAggregation(index, type, field, precision);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Path("/search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse search(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("from") Integer from,
			@QueryParam("limit") Integer limit,
			@QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation,
			@QueryParam("top") Double top,
			@QueryParam("left") Double left,
			@QueryParam("bottom") Double bottom,
			@QueryParam("right") Double right,
			MapSearchQuery query) {
		
		if(onlyFilteredAggregation != null && onlyFilteredAggregation == true &&
				(top == null || bottom == null || left == null || right == null))
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("Bounds not specified for filtering").build());
		
		MapBounds bounds = null;
		if(top != null && left != null && bottom != null && right != null)
			bounds = new MapBounds(top, left, bottom, right);
		
		if(bounds != null && geoAggregationField == null)
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("Location field not specified for bounds").build());

		try {
			return esService.search(index, type, query, from, limit, sortOn, sortType,
					geoAggregationField, geoAggegationPrecision, onlyFilteredAggregation, bounds);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/download/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String download(@PathParam("index") String index,
			@PathParam("type") String type,
			@QueryParam("filePath") String filePath,
			@QueryParam("fileType") String fileType,
			MapSearchQuery query) {
		try {
			return esService.downloadSearch(index, type, query, filePath, fileType);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	

	//---------- Admin Services -------------
	
	@GET
	@Path("/mapping/{index}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument getMapping(@PathParam("index") String index) {
		
		try {
			return esAdminService.getMapping(index);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Path("/mapping/{index}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse postMapping(@PathParam("index") String index,
			MapDocument mapping) {

		String docString = String.valueOf(mapping.getDocument());
		
		try {
			return esAdminService.postMapping(index, docString);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}
	
	@POST
	@Path("/index-admin/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse createIndex(@PathParam("index") String index,
			@PathParam("type") String type) {
		
		try {
			return esAdminService.createIndex(index, type);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
