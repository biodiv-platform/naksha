package com.strandls.naksha.utils;

/**
 * Calculates distance between two lat long coordinate points
 * @author mukund
 *
 */
public class GeoDistance {

	public static double distance(double fromLong, double fromLat, double toLong, double toLat) {
	    
	    double dLat = degreesToRadians(toLat - fromLat);
	    double dLon = degreesToRadians(toLong - fromLong);
	    double lat1 = degreesToRadians(fromLat);
	    double lat2 = degreesToRadians(toLat);

	    double a = Math.pow(Math.sin(dLat / 2), 2) +
	          Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);

	    return radiansToLengthKm(2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
	}
	
	public static double degreesToRadians(double degrees) {
	    return (degrees % 360) * Math.PI/180;
	}
	
	public static double radiansToDegrees(double radians) {
	    return (radians % (2*Math.PI)) * 180/Math.PI;
	}
	
	public static double radiansToLengthKm(double radians) {
	    return radians * 6371008.8/1000;
	}

}
