package com.strandls.naksha.binning.services;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

/**
 * Guice binding module
 * @author mukund
 *
 */
public class BinningModule extends ServletModule  {
	
	@Override
	protected void configureServlets() {
		
		bind(BinningService.class).in(Singleton.class);
		bind(GeojsonService.class).in(Singleton.class);
	}
}