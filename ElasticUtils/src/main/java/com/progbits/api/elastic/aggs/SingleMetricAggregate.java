package com.progbits.api.elastic.aggs;

import com.progbits.api.elastic.query.JsonFunctions;

/**
 * Allows for Multiple Types of Aggregates that return a SINGLE value Example:
 * sum, max, min, avg
 *
 * @author scarr
 */
public class SingleMetricAggregate implements Aggregate {

	private String aggName;
	private String aggType;
	private String fieldName;

	public SingleMetricAggregate() {
	}

	public SingleMetricAggregate(String aggName, String aggType,
			String fieldName) {
		this.aggName = aggName;
		this.aggType = aggType;
		this.fieldName = fieldName;
	}

	public String getAggName() {
		return aggName;
	}

	public void setAggName(String aggName) {
		this.aggName = aggName;
	}

	public String getAggType() {
		return aggType;
	}

	public void setAggType(String aggType) {
		this.aggType = aggType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, aggName);

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, aggType);

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, "field");
		JsonFunctions.processFieldValue(sb, fieldName, false);

		sb.append(" } ");
		sb.append(" } ");
	}

}
