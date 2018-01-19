package com.strandls.naksha.es.services;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.strandls.naksha.es.models.MapResponse;
import com.strandls.naksha.es.services.impl.ElasticSearchServiceImpl;

public class ElasticSearchServiceTest {

	@Test
	@Ignore
	public void termSearchTest() throws IOException {
		ElasticSearchServiceImpl service = new ElasticSearchServiceImpl();
		
		MapResponse response = service.termSearch("biodiv", "observations", "group_id", "837", null, null, null, null, null, null);
		Assert.assertNotEquals(0, response.getTotalDocuments());
	}
}
