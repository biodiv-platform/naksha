package com.strandls.naksha.geoserver.models;

public class GeoserverLayerStyles {

	private String styleName;
	
	private String styleTitle;

	public GeoserverLayerStyles(String styleId, String styleName) {
		super();
		this.styleName = styleId;
		this.styleTitle = styleName;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public String getStyleTitle() {
		return styleTitle;
	}

	public void setStyleTitle(String styleTitle) {
		this.styleTitle = styleTitle;
	}
	
}
