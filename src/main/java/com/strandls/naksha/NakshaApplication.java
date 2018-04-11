package com.strandls.naksha;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

import org.elasticsearch.client.ElasticsearchClient;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import com.strandls.naksha.controllers.BinningController;
import com.strandls.naksha.controllers.GeoController;
import com.strandls.naksha.controllers.GeoserverController;
import com.strandls.naksha.controllers.NakshaController;
import com.strandls.naksha.es.ESClientProvider;
import com.strandls.naksha.es.services.api.ElasticAdminSearchService;
import com.strandls.naksha.es.services.api.ElasticSearchGeoService;
import com.strandls.naksha.es.services.api.ElasticSearchService;
import com.strandls.naksha.es.services.impl.ElasticAdminSearchServiceImpl;
import com.strandls.naksha.es.services.impl.ElasticSearchGeoServiceImpl;
import com.strandls.naksha.es.services.impl.ElasticSearchServiceImpl;

/**
 * 
 * @author mukund
 *
 */
@ApplicationPath("")
public class NakshaApplication extends ResourceConfig {

	public NakshaApplication() {
		register(NakshaController.class);
		register(BinningController.class);
		register(GeoController.class);
		register(GeoserverController.class);
		register(NakshaResponseFilter.class);

		register(new AbstractBinder() {

			@Override
			protected void configure() {
				bind(ESClientProvider.class).in(Singleton.class);
				bind(ElasticsearchClient.class).in(Singleton.class);
				bind(ElasticAdminSearchServiceImpl.class).to(ElasticAdminSearchService.class).in(Singleton.class);
				bind(ElasticSearchServiceImpl.class).to(ElasticSearchService.class).in(Singleton.class);
				bind(ElasticSearchGeoServiceImpl.class).to(ElasticSearchGeoService.class).in(Singleton.class);
			}
		});
	}

}
