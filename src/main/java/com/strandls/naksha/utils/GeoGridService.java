package com.strandls.naksha.utils;

/**
 * Divide an area into various types of grids
 * @author mukund
 *
 */
public class GeoGridService {

	public static double[][][] squareGrid(double east, double west, double north, double south, double cellSide) {

		double xFraction = cellSide / GeoDistance.distance(west, south, east, south);
		double cellWidth = xFraction * (east - west);
		double yFraction = cellSide / GeoDistance.distance(west, south, west, north);
		double cellHeight = yFraction * (north - south);

		double bboxWidth = (east - west);
		double bboxHeight = (north - south);

		int columns = (int) Math.floor(bboxWidth / cellWidth);
		int rows = (int) Math.floor(bboxHeight / cellHeight);

		double deltaX = (bboxWidth - columns * cellWidth) / 2;
		double deltaY = (bboxHeight - rows * cellHeight) / 2;

		double currentX = west + deltaX;

		double[][][] coordinatesList = new double[columns * rows][5][2];

		for (int column = 0; column < columns; column++) {

			double currentY = south + deltaY;
			for (int row = 0; row < rows; row++) {

				double[][] coordinates = new double[5][2];
				coordinates[0][0] = currentX;
				coordinates[0][1] = currentY;

				coordinates[1][0] = currentX;
				coordinates[1][1] = currentY + cellHeight;

				coordinates[2][0] = currentX + cellWidth;
				coordinates[2][1] = currentY + cellHeight;

				coordinates[3][0] = currentX + cellWidth;
				coordinates[3][1] = currentY;

				coordinates[4][0] = currentX;
				coordinates[4][1] = currentY;

				coordinatesList[rows * column + row] = coordinates;

				currentY += cellHeight;
			}
			currentX += cellWidth;
		}

		return coordinatesList;
	}

}
