package com.strandls.naksha.es.models;

import java.util.HashMap;
import java.util.Map;

public class AggregationResponse {

	private HashMap<Object, Long> groupAggregation;

	public AggregationResponse() {
		
	}
	public AggregationResponse(HashMap<Object, Long> groupAggregation) {
		super();
		this.groupAggregation = groupAggregation;
	}

	public HashMap<Object, Long> getGroupAggregation() {
		return groupAggregation;
	}

	public void setGroupAggregation(HashMap<Object, Long> groupAggregation) {
		this.groupAggregation = groupAggregation;
	}
	

}
