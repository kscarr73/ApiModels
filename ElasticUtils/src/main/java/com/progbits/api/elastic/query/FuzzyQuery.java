package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class FuzzyQuery implements Query {

	private String fieldName = null;
	private Object fieldValue = null;

	public FuzzyQuery() {
	}

	public FuzzyQuery(String fieldName, Object fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(Object fieldValue) {
		this.fieldValue = fieldValue;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "fuzzy");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, fieldName);

		JsonFunctions.processFieldValue(sb, fieldValue);

		sb.append(" } ");
	}
}
