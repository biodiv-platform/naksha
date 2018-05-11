package com.strandls.naksha.es.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.models.query.MapSearchQuery;
import com.strandls.naksha.es.services.api.ElasticSearchDownloadService;
import com.strandls.naksha.utils.Utils;

/**
 * Implementation of {@link ElasticSearchDownloadService}
 * 
 * @author mukund
 *
 */
public class ElasticSearchDownloadServiceImpl extends ElasticSearchQueryUtil implements ElasticSearchDownloadService {

	private final Logger logger = LoggerFactory.getLogger(ElasticSearchDownloadServiceImpl.class);

	@Inject
	private ElasticSearchClient client;

	public enum DownloadFileType {
		CSV,
		JSON
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.strandls.naksha.es.services.api.ElasticSearchDownloadService#
	 * downloadSearch(java.lang.String, java.lang.String,
	 * com.strandls.naksha.es.models.query.MapSearchQuery, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String downloadSearch(String index, String type, MapSearchQuery query, String filePath, String fileType)
			throws IOException {
		logger.info("Download request received for index: {}, type: {}, fileType: {}", index, type, fileType);

		SearchRequest searchRequest = getDownloadSearchRequest(query, index, type);
		DownloadFileType downloadFileType = fileType != null ? DownloadFileType.valueOf(fileType)
				: DownloadFileType.CSV;

		File zipFile = new File(filePath + File.separator + System.currentTimeMillis() + ".zip");

		try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {

			ZipEntry e = new ZipEntry("download_search." + downloadFileType.toString().toLowerCase());
			zipOut.putNextEntry(e);

			if (DownloadFileType.CSV == downloadFileType) {
				downloadCSV(searchRequest, zipOut);
			}
			else if (DownloadFileType.JSON == downloadFileType) {
				downloadJson(searchRequest, zipOut);
			}
		}

		logger.info("Download completed for index: {}, type: {}, file: {}", index, type, zipFile.getAbsolutePath());

		return zipFile.getAbsolutePath();
	}

	private void downloadJson(SearchRequest searchRequest, ZipOutputStream out) throws IOException {

		SearchResponse searchResponse = client.search(searchRequest);

		do {

			for (SearchHit hit : searchResponse.getHits().getHits())
				out.write(hit.getSourceAsString().getBytes());

			SearchScrollRequest request = new SearchScrollRequest(searchResponse.getScrollId());
			request.scroll(new TimeValue(60000));

			searchResponse = client.searchScroll(request);

		} while (searchResponse.getHits().getHits().length != 0);

	}

	private void downloadCSV(SearchRequest searchRequest, ZipOutputStream out) throws IOException {

		SearchResponse searchResponse = client.search(searchRequest);

		boolean first = true;
		Set<String> headerSet = new HashSet<>();
		List<Object> values;
		do {
			for (SearchHit hit : searchResponse.getHits().getHits()) {
				Map<String, Object> resultMap = hit.getSourceAsMap();

				if (headerSet.isEmpty())
					headerSet = hit.getSourceAsMap().keySet();

				if (first)
					out.write(Utils.getCsvBytes(new ArrayList<Object>(headerSet)));

				values = new ArrayList<>();
				for (String key : headerSet) {
					values.add(resultMap.get(key));
				}

				out.write(Utils.getCsvBytes(values));
				first = false;
			}

			SearchScrollRequest request = new SearchScrollRequest(searchResponse.getScrollId());
			request.scroll(new TimeValue(60000));

			searchResponse = client.searchScroll(request);
		} while (searchResponse.getHits().getHits().length != 0);

	}

	private SearchRequest getDownloadSearchRequest(MapSearchQuery query, String index, String type) {
		BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(query);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(boolQueryBuilder);
		sourceBuilder.size(5000);
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.types(type);
		searchRequest.source(sourceBuilder);
		searchRequest.scroll(new TimeValue(60000));

		return searchRequest;
	}
}
