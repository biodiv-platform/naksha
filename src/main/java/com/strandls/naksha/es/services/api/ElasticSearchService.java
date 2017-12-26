package com.strandls.naksha.es.services.api;

import java.io.IOException;
import java.util.List;

import com.strandls.naksha.es.models.MapBoolQuery;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapRangeQuery;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSearchQuery;

/**
 * All the service(excluding admin services) supported by map app
 * 
 * @author mukund
 */
public interface ElasticSearchService {

	/**
	 * Creates a document in es
	 * 
	 * @param index
	 *            the index of the document
	 * @param type
	 *            the type of the document
	 * @param documentId
	 *            unique id of the document
	 * @param document
	 *            the json document to be added
	 * @return {@link MapResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse create(String index, String type, String documentId, String document) throws IOException;

	/**
	 * Fetches a document in es
	 * 
	 * @param index
	 *            the index of the document
	 * @param type
	 *            the type of the document
	 * @param documentId
	 *            unique id of the document
	 * @return {@link MapDocument} the document if any
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapDocument fetch(String index, String type, String documentId) throws IOException;

	/**
	 * Updates a document in es if it exists
	 * 
	 * @param index
	 *            the index of the document
	 * @param type
	 *            the type of the document
	 * @param documentId
	 *            unique id of the document
	 * @param document
	 *            the json document to be updated
	 * @return {@link MapResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse update(String index, String type, String documentId, String document) throws IOException;

	/**
	 * Deletes a document in es if it exists
	 * 
	 * @param index
	 *            the index of the document
	 * @param type
	 *            the type of the document
	 * @param documentId
	 *            unique id of the document
	 * @return {@link MapResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse delete(String index, String type, String documentId) throws IOException;

	/**
	 * Bulk upload the the documents in the form of json array
	 * 
	 * @param index
	 *            the index of the documents
	 * @param type
	 *            the type of the documents
	 * @param jsonArray
	 *            the json array of documents needed to be uploaded
	 * @return list of {@link MapResponse} for individual json documents
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapResponse> bulkUpload(String index, String type, String jsonArray) throws IOException;

	/**
	 * Search for a particular key value pair
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param key
	 *            the key of interest
	 * @param value
	 *            the value of the key
	 * @param from
	 *            optional
	 * @param limit
	 *            optional
	 * @return list of {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapDocument> termSearch(String index, String type, String key, String value, Integer from, Integer limit)
			throws IOException;

	/**
	 * Search of the form that a key can have multiple values. List of such queries
	 * to be run. There is an "AND" between individual such queries.
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param queries
	 *            list of {@link MapBoolQuery} queries
	 * @param from
	 *            optional
	 * @param limit
	 *            optional
	 * @return list of {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapDocument> boolSearch(String index, String type, List<MapBoolQuery> queries, Integer from, Integer limit)
			throws IOException;

	/**
	 * Search of the form that a key can have its value in a range. List of such
	 * queries to be run. There is an "AND" between individual such queries.
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param queries
	 *            list of {@link MapRangeQuery} queries
	 * @param from
	 *            optional
	 * @param limit
	 *            optional
	 * @return list of {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapDocument> rangeSearch(String index, String type, List<MapRangeQuery> queries, Integer from, Integer limit)
			throws IOException;

	/**
	 * Search with combination of boolSeach and rangeSearch.
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param query
	 *            the query
	 * @param from
	 *            optional
	 * @param limit
	 *            optional
	 * @return list of {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapDocument> search(String index, String type, MapSearchQuery query, Integer from, Integer limit)
			throws IOException;

	/**
	 * Geohash aggregation search on a geo_point field.
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param field
	 *            the field on which aggregation needs to be performed.
	 * @param precision
	 *            the precision raning between 1 to 12 for aggregation.
	 * @return list of {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapDocument geohashAggregation(String index, String type, String field, Integer precision) throws IOException;

}
