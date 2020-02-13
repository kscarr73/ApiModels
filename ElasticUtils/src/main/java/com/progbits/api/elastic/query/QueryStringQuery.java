package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class QueryStringQuery implements Query {

	private String fieldName = null;
	private String queryString = null;

	public QueryStringQuery() {
	}

	public QueryStringQuery(String fieldName, String queryString) {
		this.fieldName = fieldName;
		this.queryString = queryString;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "query_string");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, "default_field");

		JsonFunctions.processFieldValue(sb, fieldName);

		sb.append(",");

		JsonFunctions.processFieldName(sb, "query");

		JsonFunctions.processFieldValue(sb, queryString);

		sb.append(" } ");
	}
}
