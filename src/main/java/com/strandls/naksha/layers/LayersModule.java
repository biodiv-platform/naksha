package com.strandls.naksha.layers;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.layers.services.LayerService;

public class LayersModule extends ServletModule  {
	
	@Override
	protected void configureServlets() {
		bind(LayerService.class).in(Singleton.class);
	}
}