package com.strandls.naksha.dao.jdbc;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.dao.LayerDAO;

public class DAOModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(LayerDAO.class).to(LayerDAOJDBC.class).in(Singleton.class);
	}
}