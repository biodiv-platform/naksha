package com.strandls.naksha.es.models;

public class MapGeoPoint {

	private double lat;
	private double lon;

	public MapGeoPoint() {}

	public MapGeoPoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public MapGeoPoint(String value) {
		int comma = value.indexOf(',');
		if (comma != -1) {
			lat = Double.parseDouble(value.substring(0, comma).trim());
			lon = Double.parseDouble(value.substring(comma + 1).trim());
		}
	}

	public double getLat() {
		return this.lat;
	}

	public double getLon() {
		return this.lon;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MapGeoPoint geoPoint = (MapGeoPoint) o;

		return Double.compare(geoPoint.lat, lat) == 0 && Double.compare(geoPoint.lon, lon) == 0;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
		result = Long.hashCode(temp);
		temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
		result = 31 * result + Long.hashCode(temp);
		return result;
	}

	@Override
	public String toString() {
		return lat + ", " + lon;
	}
}
