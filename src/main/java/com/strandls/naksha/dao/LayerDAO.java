package com.strandls.naksha.dao;

import java.util.List;

import com.strandls.naksha.layers.models.LayerAttributes;

/**
 * DAO for map layers that are stored in post gis
 * @author mukund
 *
 */
public interface LayerDAO {

	/**
	 * Get all the attributes present in the layer
	 * @param layerName
	 * @return
	 */
	List<LayerAttributes> getLayerAttributes(String layerName);

	/**
	 * Get layer names associated with a tag
	 * @return
	 */
	List<String> getLayerNamesWithTag(String tag);
}
