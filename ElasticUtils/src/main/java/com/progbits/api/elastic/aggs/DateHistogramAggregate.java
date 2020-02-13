package com.progbits.api.elastic.aggs;

import com.progbits.api.elastic.query.JsonFunctions;

/**
 *
 * @author scarr
 */
public class DateHistogramAggregate implements Aggregate {

	private String aggName;
	private String aggType = "date_histogram";
	private String fieldName;
	private String interval;

	public DateHistogramAggregate() {
	}

	public DateHistogramAggregate(String aggName, String fieldName,
			String interval) {
		this.aggName = aggName;
		this.fieldName = fieldName;
		this.interval = interval;
	}

	public String getAggName() {
		return aggName;
	}

	public void setAggName(String aggName) {
		this.aggName = aggName;
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

		sb.append(", ");

		JsonFunctions.processFieldName(sb, "interval");
		JsonFunctions.processFieldValue(sb, interval, false);

		sb.append(" } ");
		sb.append(" } ");
	}
}
