package com.strandls.naksha.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.naksha.es.models.MapBoundParams;
import com.strandls.naksha.es.models.MapBounds;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryResponse;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSearchParams;
import com.strandls.naksha.es.models.MapSortType;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;
import com.strandls.naksha.es.services.api.ElasticAdminSearchService;
import com.strandls.naksha.es.services.api.ElasticSearchDownloadService;
import com.strandls.naksha.es.services.api.ElasticSearchService;

/**
 * 
 * @author mukund
 *
 */
@Path("services")
public class NakshaController {

	@Inject
	public ElasticSearchService elasticSearchService;

	@Inject
	public ElasticAdminSearchService elasticAdminSearchService;

	@Inject
	public ElasticSearchDownloadService elasticSearchDownloadService;

	private final Logger logger = LoggerFactory.getLogger(NakshaController.class);

	@POST
	@Path("/data/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse create(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, MapDocument document) {

		String docString = String.valueOf(document.getDocument());
		try {
			new ObjectMapper().readValue(docString, Map.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}

		try {
			return elasticSearchService.create(index, type, documentId, docString);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path("/data/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument fetch(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {

		try {
			return elasticSearchService.fetch(index, type, documentId);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@PUT
	@Path("/data/{index}/{type}/{documentId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse update(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId, Map<String, Object> document) {

		try {
			return elasticSearchService.update(index, type, documentId, document);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@DELETE
	@Path("/data/{index}/{type}/{documentId}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse delete(@PathParam("index") String index, @PathParam("type") String type,
			@PathParam("documentId") String documentId) {

		try {
			return elasticSearchService.delete(index, type, documentId);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}

	}

	@POST
	@Path("/bulk-upload/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MapQueryResponse> bulkUpload(@PathParam("index") String index, @PathParam("type") String type,
			String jsonArray) {

		try {
			return elasticSearchService.bulkUpload(index, type, jsonArray);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@PUT
	@Path("/bulk-update/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MapQueryResponse> bulkUpdate(@PathParam("index") String index, @PathParam("type") String type,
			List<Map<String, Object>> updateDocs) {

		if (updateDocs == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("No documents to update").build());

		for (Map<String, Object> doc : updateDocs) {
			if (!doc.containsKey("id"))
				throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
						.entity("Id not present of the document to be updated").build());
		}

		try {
			return elasticSearchService.bulkUpdate(index, type, updateDocs);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/term-search/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse search(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("key") String key, @QueryParam("value") String value,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision, MapSearchParams searchParams) {

		if (key == null || value == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("key or value not specified").build());

		try {
			return elasticSearchService.termSearch(index, type, key, value, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/terms-search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse boolSearch(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("from") Integer from, @QueryParam("limit") Integer limit, @QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType, @QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision, List<MapBoolQuery> query) {

		try {
			MapSearchParams searchParams = new MapSearchParams(from, limit, sortOn, sortType);
			return elasticSearchService.boolSearch(index, type, query, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/range-search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse rangeSearch(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("from") Integer from, @QueryParam("limit") Integer limit, @QueryParam("sortOn") String sortOn,
			@QueryParam("sortType") MapSortType sortType, @QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision, List<MapRangeQuery> query) {

		try {
			MapSearchParams searchParams = new MapSearchParams(from, limit, sortOn, sortType);
			return elasticSearchService.rangeSearch(index, type, query, searchParams, geoAggregationField,
					geoAggegationPrecision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@GET
	@Path("/geohash-aggregation/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument geohashAggregation(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoAggregationField") String field, @QueryParam("geoAggegationPrecision") Integer precision) {

		if (field == null || precision == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Field or precision not specified").build());
		if (precision < 1 || precision > 12)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Precision value must be between 1 and 12").build());

		try {
			return elasticSearchService.geohashAggregation(index, type, field, precision);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/terms-aggregation/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument termsAggregation(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("field") String field, @QueryParam("subField") String subField,
			@QueryParam("size") Integer size, @QueryParam("locationField") String locationField, MapSearchQuery query) {

		if (field == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Aggregation field cannot be empty").build());

		MapSearchParams searchParams = query.getSearchParams();
		MapBoundParams boundParams = searchParams.getMapBoundParams();
		MapBounds mapBounds = null;
		if (boundParams != null)
			mapBounds = boundParams.getBounds();

		if ((locationField != null && mapBounds == null) || (locationField == null && mapBounds != null))
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Incomplete map bounds request").build());

		try {
			return elasticSearchService.termsAggregation(index, type, field, subField, size, locationField, query);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/search/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapResponse search(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoAggregationField") String geoAggregationField,
			@QueryParam("geoAggegationPrecision") Integer geoAggegationPrecision,
			@QueryParam("onlyFilteredAggregation") Boolean onlyFilteredAggregation, MapSearchQuery query) {

		MapSearchParams searchParams = query.getSearchParams();
		MapBoundParams boundParams = searchParams.getMapBoundParams();
		MapBounds bounds = null;
		if (boundParams != null)
			bounds = boundParams.getBounds();

		if (onlyFilteredAggregation != null && onlyFilteredAggregation && bounds == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Bounds not specified for filtering").build());

		if (bounds != null && geoAggregationField == null)
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity("Location field not specified for bounds").build());

		try {
			return elasticSearchService.search(index, type, query, geoAggregationField, geoAggegationPrecision,
					onlyFilteredAggregation);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/download/{index}/{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String download(@PathParam("index") String index, @PathParam("type") String type,
			@QueryParam("geoField") String geoField, @QueryParam("filePath") String filePath,
			@QueryParam("fileType") String fileType, MapSearchQuery query) {
		try {
			return elasticSearchDownloadService.downloadSearch(index, type, query, geoField, filePath, fileType);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	// ---------- Admin Services -------------

	@GET
	@Path("/mapping/{index}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapDocument getMapping(@PathParam("index") String index) {

		try {
			return elasticAdminSearchService.getMapping(index);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/mapping/{index}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse postMapping(@PathParam("index") String index, MapDocument mapping) {

		String docString = String.valueOf(mapping.getDocument());

		try {
			return elasticAdminSearchService.postMapping(index, docString);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

	@POST
	@Path("/index-admin/{index}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public MapQueryResponse createIndex(@PathParam("index") String index, @PathParam("type") String type) {

		try {
			return elasticAdminSearchService.createIndex(index, type);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
	}

}
