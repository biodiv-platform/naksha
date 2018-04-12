package com.strandls.naksha.binning.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.strandls.naksha.binning.models.Feature;
import com.strandls.naksha.binning.models.Geojson;
import com.strandls.naksha.binning.models.GeojsonData;
import com.strandls.naksha.binning.models.Geometry;
import com.strandls.naksha.es.ElasticSearchClient;

/**
 * Services for {@link GeojsonData}
 * @author mukund
 *
 */
public class GeojsonService {

	@Inject
	private ElasticSearchClient client;

	public GeojsonData getGeojsonData(String index, String type, String geoField, double[][][] coordinatesList) throws IOException {

		Collection<Feature> features = new ArrayList<>(coordinatesList.length);

		long max_count = 0;
		long min_count = 0;
		
		for (int i = 0; i < coordinatesList.length; i++) {
			
			// geometry
			double[][][] coordinates = new double[1][5][2];
			coordinates[0] = coordinatesList[i];
			Geometry geometry = new Geometry("Polygon", coordinates);

			// properties
			Map<String, Object> properties = new HashMap<>();
			GeoBoundingBoxQueryBuilder query = QueryBuilders.geoBoundingBoxQuery(geoField)
					.setCorners(coordinates[0][1][1], coordinates[0][0][0], coordinates[0][0][1], coordinates[0][2][0]);
			long count = querySearch(index, type, query);
			properties.put("doc_count", count);
			
			features.add(new Feature(geometry, properties));
			
			max_count = Math.max(max_count, count);
			min_count = Math.min(min_count, count);
		}

		Geojson geojson = new Geojson(features);
		return new GeojsonData(geojson, max_count, min_count);
	}

	private long querySearch(String index, String type, QueryBuilder query) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.query(query);

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest);

		return searchResponse.getHits().getTotalHits();
	}

}
