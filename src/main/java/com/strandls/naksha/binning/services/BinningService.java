package com.strandls.naksha.binning.services;

import java.io.IOException;

import javax.inject.Inject;

import com.strandls.naksha.binning.models.GeojsonData;
import com.strandls.naksha.utils.GeoGridService;

/**
 * Bin the geographical data in different shapes and size.
 * 
 * @author mukund
 *
 */
public class BinningService {

	@Inject
	private GeojsonService geojsonService;

	public GeojsonData squareBin(String index, String type, String geoField, Double east, Double west, Double north,
			Double south, Double cellSideKm) throws IOException {

		double[][][] coordinatesList = GeoGridService.squareGrid(east, west, north, south, cellSideKm);

		return geojsonService.getGeojsonData(index, type, geoField, coordinatesList);
	}

}
