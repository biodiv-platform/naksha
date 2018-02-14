package com.strandls.naksha.es.models;

/**
 * Bounds for a map in coordinate system
 * @author mukund
 *
 */
public class MapBounds {

	private double top;
	
	private double left;
	
	private double bottom;
	
	private double right;

	public MapBounds(double top, double left, double bottom, double right) {
		super();
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "MapBounds [top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right + "]";
	}
}
