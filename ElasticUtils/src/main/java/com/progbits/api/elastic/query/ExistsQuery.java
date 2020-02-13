package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class ExistsQuery implements Query {

	private String fieldName = null;

	public ExistsQuery() {
	}

	public ExistsQuery(String fieldName) {
		this.fieldName = fieldName;
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
		JsonFunctions.processFieldName(sb, "exists");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, "field");

		JsonFunctions.processFieldValue(sb, fieldName, false);

		sb.append(" } ");
	}
}
