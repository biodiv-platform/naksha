package com.strandls.naksha.es.services;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.strandls.naksha.es.models.MapDocument;
import com.strandls.naksha.es.services.impl.ElasticSearchServiceImpl;

public class ElasticSearchServiceTest {

	@Test
	public void termSearchTest() throws IOException {
		ElasticSearchServiceImpl service = new ElasticSearchServiceImpl();
		
		List<MapDocument> list = service.termSearch("biodiv", "obs100", "group_id", "837", null, null);
		System.out.println(list.size());
		Assert.assertNotEquals(0, list.size());
	}
}
