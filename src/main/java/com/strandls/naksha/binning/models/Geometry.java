package com.strandls.naksha.binning.models;

/**
 * A geometry in {@link Geojson}
 * 
 * @author mukund
 *
 */
public class Geometry {

	private String type;

	private double[][][] coordinates = new double[1][5][2];

	public Geometry(String type, double[][][] coordinates) {
		super();
		this.type = type;
		this.coordinates = coordinates;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double[][][] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(double[][][] coordinates) {
		this.coordinates = coordinates;
	}
}
