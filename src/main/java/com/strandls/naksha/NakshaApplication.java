package com.strandls.naksha;

import javax.ws.rs.ApplicationPath;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.google.inject.Injector;

/**
 * 
 * @author mukund
 *
 */
@ApplicationPath("")
public class NakshaApplication extends ResourceConfig {

	public NakshaApplication() {

		register(new ContainerLifecycleListener() {

			@Override
			public void onStartup(Container container) {

				ServletContainer servletContainer = (ServletContainer) container;

				Injector injector = (Injector) servletContainer.getServletContext()
						.getAttribute(Injector.class.getName());

				servletContainer.reload(new ResourceConfig()
						.packages("com.strandls.naksha.controllers")
						.register(MultiPartFeature.class));

				ServiceLocator serviceLocator = container.getApplicationHandler().getServiceLocator();

				GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

				GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);

				guiceBridge.bridgeGuiceInjector(injector);
			}

			@Override
			public void onShutdown(Container container) {
				// do nothing

			}

			@Override
			public void onReload(Container container) {
				// do nothing

			}
		});
	}
}