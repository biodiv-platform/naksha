package com.strandls.naksha.es.services.impl;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.es.services.api.ElasticAdminSearchService;
import com.strandls.naksha.es.services.api.ElasticSearchGeoService;
import com.strandls.naksha.es.services.api.ElasticSearchService;

/**
 * Guice binding module
 * @author mukund
 *
 */
public class ESModule extends ServletModule  {
	
	@Override
	protected void configureServlets() {
		
		bind(ElasticAdminSearchService.class).to(ElasticAdminSearchServiceImpl.class).in(Singleton.class);
		bind(ElasticSearchService.class).to(ElasticSearchServiceImpl.class).in(Singleton.class);
		bind(ElasticSearchGeoService.class).to(ElasticSearchGeoServiceImpl.class).in(Singleton.class);

	}
}