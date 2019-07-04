package com.strandls.naksha.es.services.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.strandls.naksha.es.models.AggregationResponse;
import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapQueryResponse;
import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.models.MapSearchParams;
import com.strandls.naksha.es.models.query.MapBoolQuery;
import com.strandls.naksha.es.models.query.MapQuery;
import com.strandls.naksha.es.models.query.MapRangeQuery;
import com.strandls.naksha.es.models.query.MapSearchQuery;

/**
 * All search services supported by map app
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
	 * @return {@link MapQueryResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapQueryResponse create(String index, String type, String documentId, String document) throws IOException;

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
	 *            the document in the form of key-value pairs to be updated
	 * @return {@link MapQueryResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapQueryResponse update(String index, String type, String documentId, Map<String, Object> document)
			throws IOException;

	/**
	 * Deletes a document in es if it exists
	 * 
	 * @param index
	 *            the index of the document
	 * @param type
	 *            the type of the document
	 * @param documentId
	 *            unique id of the document
	 * @return {@link MapQueryResponse} containing the status of the operation
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapQueryResponse delete(String index, String type, String documentId) throws IOException;

	/**
	 * Bulk upload the the documents that are in the form of json array
	 * 
	 * @param index
	 *            the index of the documents
	 * @param type
	 *            the type of the documents
	 * @param jsonArray
	 *            the json array of documents needed to be uploaded
	 * @return list of {@link MapQueryResponse} for individual json documents
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapQueryResponse> bulkUpload(String index, String type, String jsonArray) throws IOException;

	/**
	 * Bulk update the the documents that are provided as list of map of new values
	 * for fields
	 *
	 * @param index
	 *            the index of the documents
	 * @param type
	 *            the type of the documents
	 * @param updateDocs
	 *            List of map of fields and corresponding new values
	 * @return list of {@link MapQueryResponse} for individual json documents
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	List<MapQueryResponse> bulkUpdate(String index, String type, List<Map<String, Object>> updateDocs)
			throws IOException;

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
	 * @param searchParams
	 *            {@link MapSearchParams} search parameters
	 * @param geoAggregationField
	 *            the geo_point field on which geohash aggregation is required
	 * @param geoAggegationPrecision
	 *            the precision for geohash aggregation, default is 1
	 * @return {@link MapResponse}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse termSearch(String index, String type, String key, String value, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException;

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
	 * @param searchParams
	 *            {@link MapSearchParams} search parameters
	 * @param geoAggregationField
	 *            the geo_point field on which geohash aggregation is required
	 * @param geoAggegationPrecision
	 *            the precision for geohash aggregation, default is 1
	 * @return {@link MapResponse}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse boolSearch(String index, String type, List<MapBoolQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException;

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
	 * @param searchParams
	 *            {@link MapSearchParams} search parameters
	 * @param geoAggregationField
	 *            the geo_point field on which geohash aggregation is required
	 * @param geoAggegationPrecision
	 *            the precision for geohash aggregation, default is 1
	 * @return {@link MapResponse}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse rangeSearch(String index, String type, List<MapRangeQuery> queries, MapSearchParams searchParams,
			String geoAggregationField, Integer geoAggegationPrecision) throws IOException;

	/**
	 * Search with combination of boolSeach and rangeSearch.
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param query
	 *            the query
	 * @param geoAggregationField
	 *            the geo_point field on which geohash aggregation is required
	 * @param geoAggegationPrecision
	 *            the precision for geohash aggregation, default is 1
	 * @param onlyFilteredAggregation
	 *            if true give aggregation result only for the bounds specified
	 * @param termsAggregationField
	 *            if present find terms aggregation based on this field
	 * @return {@link MapResponse}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapResponse search(String index, String type, MapSearchQuery query, String geoAggregationField,
			Integer geoAggegationPrecision, Boolean onlyFilteredAggregation, String termsAggregationField)
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
	 * @return {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapDocument geohashAggregation(String index, String type, String field, Integer precision) throws IOException;

	/**
	 * Terms aggregation
	 * 
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param field
	 *            the field on which aggregation needs to be performed.
	 * @param subField
	 *            the field on which sub-aggregation needs to be performed.
	 * @param size
	 *            the limit on the number of output buckets
	 * @param locationField
	 *            the field representing map point
	 * @param query
	 *            optional {@link MapQuery}
	 * @return {@link MapDocument}
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	MapDocument termsAggregation(String index, String type, String field, String subField, Integer size,
			String locationField, MapSearchQuery query) throws IOException;
	
	/**
	 * 
	 * @param index
	 *  	the index in which to search
	 * @param type
	 * 		the type in which to search
	 * @param serachQuery
	 * 		optional
	 * @return
	 * 		{@link AggregationResponse}
	 */
	AggregationResponse aggregation(String index,String type,MapSearchQuery serachQuery,String geoAggregationField,String filter) throws IOException;
}
