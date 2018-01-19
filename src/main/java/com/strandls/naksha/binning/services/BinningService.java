package com.strandls.naksha.binning.services;

import java.io.IOException;

import com.strandls.naksha.binning.models.GeojsonData;
import com.strandls.naksha.utils.GeoGridService;

/**
 * 
 * @author mukund
 *
 */
public class BinningService {

	public GeojsonData squareBin(String index, String type, String geoField, Double east, Double west, Double north, Double south, Double cellSideKm)
			throws IOException {

		double[][][] coordinatesList = GeoGridService.squareGrid(east, west, north, south, cellSideKm);
		
		return GeojsonService.getGeojsonData(index, type, geoField, coordinatesList);
	}

}
