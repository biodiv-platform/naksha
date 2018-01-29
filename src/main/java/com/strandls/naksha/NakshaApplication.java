package com.strandls.naksha;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * 
 * @author mukund
 *
 */
@ApplicationPath("")
public class NakshaApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(com.strandls.naksha.controllers.NakshaController.class);
		classes.add(com.strandls.naksha.controllers.BinningController.class);
		classes.add(com.strandls.naksha.controllers.GeoController.class);
		classes.add(NakshaResponseFilter.class);
		return classes;
	}
}
