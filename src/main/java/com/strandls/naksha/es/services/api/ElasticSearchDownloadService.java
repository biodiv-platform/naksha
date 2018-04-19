package com.strandls.naksha.es.services.api;

import java.io.IOException;

import com.strandls.naksha.es.models.query.MapSearchQuery;

/**
 * Download functionality
 * 
 * @author mukund
 *
 */
public interface ElasticSearchDownloadService {

	/**
	 * Download the result of search in a file
	 *
	 * @param index
	 *            the index in which to search
	 * @param type
	 *            the type in which to search
	 * @param query
	 *            the query
	 * @param filePath
	 *            the filePath where the file needs to be downloaded
	 * @param fileType
	 *            the file type. Can be CSV/TSV. Default is CSV.
	 * @return Raw path of file
	 * @throws IOException
	 *             throws {@link IOException}
	 */
	String downloadSearch(String index, String type, MapSearchQuery query, String filePath, String fileType)
			throws IOException;
}
