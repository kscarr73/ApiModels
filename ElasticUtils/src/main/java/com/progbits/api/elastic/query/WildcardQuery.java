package com.progbits.api.elastic.query;

/**
 * Used for WildCard searches with * as Match Any, or ? as Match one character.
 *
 * @author scarr
 */
public class WildcardQuery implements Query {

	private String fieldName = null;
	private Object fieldValue = null;
	private boolean analyzed = true;

	public WildcardQuery() {
	}

	public WildcardQuery(String fieldName, Object fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public WildcardQuery(String fieldName, Object fieldValue, boolean analyzed) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.analyzed = analyzed;
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

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(boolean analyzed) {
		this.analyzed = analyzed;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "wildcard");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, fieldName);

		JsonFunctions.processFieldValue(sb, fieldValue, analyzed);

		sb.append(" } ");
	}

}
