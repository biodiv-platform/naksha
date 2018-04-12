package com.strandls.naksha;

import java.io.IOException;

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
import com.strandls.naksha.es.ElasticSearchClient;
import com.strandls.naksha.es.services.impl.ESModule;


public class NakshaServeletContextListener extends GuiceServletContextListener {

	private final Logger logger = LoggerFactory.getLogger(NakshaServeletContextListener.class);

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {

			@Override
			protected void configureServlets() {
				
				bind(NakshaResponseFilter.class);
				ElasticSearchClient esClient = new ElasticSearchClient(RestClient.builder(HttpHost.create(NakshaConfig.getString("es.url"))));
				bind(ElasticSearchClient.class).toInstance(esClient);
			}
		}, new ESModule(), new BinningModule());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Injector injector = (Injector) sce.getServletContext()
                 .getAttribute(Injector.class.getName());

		ElasticSearchClient elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
		if(elasticSearchClient != null) {
			try {
				elasticSearchClient.close();
			} catch (IOException e) {
				logger.error("Error closing elasticsearch client");
				e.printStackTrace();
			}
		}
		 
		super.contextDestroyed(sce);
	}

}
