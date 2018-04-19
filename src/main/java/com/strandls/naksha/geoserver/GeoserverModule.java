package com.strandls.naksha.geoserver;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class GeoserverModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(GeoServerIntegrationService.class).in(Singleton.class);
		bind(LayerService.class).in(Singleton.class);
	}
}