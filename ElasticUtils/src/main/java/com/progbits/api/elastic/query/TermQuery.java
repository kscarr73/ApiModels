package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class TermQuery implements Query {

	private String fieldName = null;
	private Object fieldValue = null;
	private boolean analyzed = true;

	public TermQuery() {
	}

	/**
	 * Set a TermQuery on an Analyzed field
	 *
	 * @param fieldName The Field Name for the Query
	 * @param fieldValue The Value for the Query
	 */
	public TermQuery(String fieldName, Object fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	/**
	 * Set a TermQuery on a field with analyzed turned TRUE/on or FALSE/off
	 *
	 * @param fieldName The Field Name for the Query
	 * @param fieldValue The Value for the Query
	 * @param analyzed TRUE/FALSE if the field is analyzed
	 */
	public TermQuery(String fieldName, Object fieldValue, boolean analyzed) {
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
		JsonFunctions.processFieldName(sb, "term");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, fieldName);

		JsonFunctions.processFieldValue(sb, fieldValue, analyzed);

		sb.append(" } ");
	}
}
