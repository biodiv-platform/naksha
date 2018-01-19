package com.strandls.naksha.es.services.api;

import java.io.IOException;

import com.strandls.naksha.es.models.MapResponse;

/**
 * Geo related services
 * 
 * @author mukund
 *
 */
public interface ElasticSearchGeoService {

	/**
	 * Get documents within geo boundary specified by top, left, bottom and right
	 * 
	 * @param index
	 *            the index to search for
	 * @param type
	 *            the type in index to search
	 * @param geoField
	 *            the field which has geo_point
	 * @param top
	 *            the top boundary
	 * @param left
	 *            the left boundary
	 * @param bottom
	 *            the bottom boundary
	 * @param right
	 *            the right boundary
	 * @return {@link MapResponse}
	 * @throws IOException
	 */
	MapResponse getGeoWithinDocuments(String index, String type, String geoField, double top, double left,
			double bottom, double right) throws IOException;
}
