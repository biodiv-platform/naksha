package com.strandls.naksha.es.services.api;

import java.io.IOException;

import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.models.MapResponse;

/**
 * All the admin serviecs supported by map app
 * 
 * @author mukund
 */
public interface ElasticAdminSearchService {

	/**
	 * Define a mapping for an index and type
	 * 
	 * @param index
	 *            the index on which mapping needs to be defined
	 * @param type
	 *            the type on which mapping needs to be defined
	 * @param mapping
	 *            the mapping
	 * @return {@link MapResponse}
	 * @throws IOException throws {@link IOException}
	 */
	MapResponse postMapping(String index, String type, String mapping) throws IOException;

	/**
	 * Get the mapping for an index
	 * 
	 * @param index
	 *            the index for which mapping is needed
	 * @return {@link MapDocument}
	 * @throws IOException throws {@link IOException}
	 */
	MapDocument getMapping(String index) throws IOException;

	/**
	 * Create an index type
	 * 
	 * @param index
	 *            the index to be created
	 * @param type
	 *            the type to be created
	 * @return {@link MapResponse}
	 * @throws IOException throws {@link IOException}
	 */
	MapResponse createIndex(String index, String type) throws IOException;

}
