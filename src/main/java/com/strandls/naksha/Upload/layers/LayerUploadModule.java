package com.strandls.naksha.Upload.layers;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.Layers.Scripts.DBexec;
import com.strandls.naksha.Layers.Scripts.Import_data;

public class LayerUploadModule extends ServletModule {

	@Override
	protected void configureServlets() {
		bind(LayerUploadService.class).in(Singleton.class);
		bind(Import_data.class).in(Singleton.class);
		bind(DBexec.class).in(Singleton.class);
	}
}
