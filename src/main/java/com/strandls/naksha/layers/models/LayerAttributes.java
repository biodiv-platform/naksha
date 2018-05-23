package com.strandls.naksha.layers.models;

/**
 * Attributes present in a layer
 * 
 * @author mukund
 *
 */
public class LayerAttributes {

	private String name;

	private String description;

	public LayerAttributes(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
