package com.strandls.naksha.geoserver;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class GeoserverModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("python");
		bind(ScriptEngine.class).toInstance(engine);
		bind(GeoServerIntegrationService.class).in(Singleton.class);
		bind(LayerService.class).in(Singleton.class);
	}
}