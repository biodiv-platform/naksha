package com.strandls.naksha;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.strandls.naksha.binning.services.BinningModule;
import com.strandls.naksha.dao.DAOFactory;
import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.services.impl.ESModule;
import com.strandls.naksha.geoserver.GeoserverModule;

public class NakshaServeletContextListener extends GuiceServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(NakshaServeletContextListener.class);

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {

			@Override
			protected void configureServlets() {
				try {
					Class.forName("org.postgresql.Driver");
					DAOFactory daoFactory = DAOFactory.getInstance();
					Connection connection = daoFactory.getConnection();
					bind(Connection.class).toInstance(connection);
				}
				catch (ClassNotFoundException e) {
					logger.error("Error finding postgresql driver.", e);
				} catch (SQLException e) {
					logger.error("Error getting database connection.", e);
				}

				bind(NakshaResponseFilter.class);
				ElasticSearchClient esClient = new ElasticSearchClient(
						RestClient.builder(HttpHost.create(NakshaConfig.getString("es.url"))));
				bind(ElasticSearchClient.class).toInstance(esClient);
			}
		}, new ESModule(), new BinningModule(), new GeoserverModule());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Injector injector = (Injector) sce.getServletContext().getAttribute(Injector.class.getName());

		ElasticSearchClient elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
		if (elasticSearchClient != null) {
			try {
				elasticSearchClient.close();
			} catch (IOException e) {
				logger.error("Error closing elasticsearch client. ", e);
			}
		}

	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    Enumeration<Driver> drivers = DriverManager.getDrivers();
	    while (drivers.hasMoreElements()) {
	        Driver driver = drivers.nextElement();
	        if (driver.getClass().getClassLoader() == cl) {
	            try {
	                logger.info("Deregistering JDBC driver {}", driver);
	                DriverManager.deregisterDriver(driver);
	            } catch (SQLException ex) {
	                logger.error("Error deregistering JDBC driver {}", driver, ex);
	            }
	        } else {
	            logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
	        }
	    }

		super.contextDestroyed(sce);
	}

}
