package com.strandls.naksha.es.models;

/**
 * Search parameters for map query
 * 
 * @author mukund
 *
 */
public class MapSearchParams {

	/**
	 * offset in response
	 */
	private Integer from;
	/**
	 * limit in response
	 */
	private Integer limit;
	/**
	 * Field on which sorting is required, if any
	 */
	private String sortOn;
	/**
	 * Sorting type
	 */
	private MapSortType sortType;
	/**
	 * the bounds in which the search is required
	 */
	private MapBounds mapBounds;

	public MapSearchParams(Integer from, Integer limit, String sortOn, MapSortType sortType) {
		super();
		this.from = from;
		this.limit = limit;
		this.sortOn = sortOn;
		this.sortType = sortType;
	}

	public MapSearchParams(Integer from, Integer limit, String sortOn, MapSortType sortType, MapBounds mapBounds) {
		super();
		this.from = from;
		this.limit = limit;
		this.sortOn = sortOn;
		this.sortType = sortType;
		this.mapBounds = mapBounds;
	}

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getSortOn() {
		return sortOn;
	}

	public void setSortOn(String sortOn) {
		this.sortOn = sortOn;
	}

	public MapSortType getSortType() {
		return sortType;
	}

	public void setSortType(MapSortType sortType) {
		this.sortType = sortType;
	}

	public MapBounds getMapBounds() {
		return mapBounds;
	}

	public void setMapBounds(MapBounds mapBounds) {
		this.mapBounds = mapBounds;
	}

	@Override
	public String toString() {
		return "MapSearchParams [from=" + from + ", limit=" + limit + ", sortOn=" + sortOn + ", sortType=" + sortType
				+ ", mapBounds=" + mapBounds.toString() + "]";
	}
	
}
