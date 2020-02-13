package com.progbits.api.elastic.query;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scarr
 */
public class TermsQuery implements Query {

	private String fieldName = null;
	private Integer minimumShouldMatch = 1;
	private boolean analyzed = true;

	private List<Object> fieldValue = new ArrayList<>();

	public TermsQuery() {
	}

	public TermsQuery(String fieldName, Integer minimumShouldMatch) {
		this.fieldName = fieldName;
		this.minimumShouldMatch = minimumShouldMatch;
	}

	public TermsQuery(String fieldName, List<Object> fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public TermsQuery(String fieldName, List<Object> fieldValue,
			  boolean analyzed) {
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

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(boolean analyzed) {
		this.analyzed = analyzed;
	}

	public Integer getMinimumShouldMatch() {
		return minimumShouldMatch;
	}

	public void setMinimumShouldMatch(Integer minimumShouldMatch) {
		this.minimumShouldMatch = minimumShouldMatch;
	}

	public Object getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(List<Object> fieldValue) {
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
		JsonFunctions.processFieldName(sb, "terms");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, fieldName);

		sb.append(" [ ");

		boolean bFirstEntry = true;

		for (Object o : fieldValue) {
			if (!bFirstEntry) {
				sb.append(", ");
			} else {
				bFirstEntry = false;
			}

			JsonFunctions.processFieldValue(sb, o, analyzed);
		}

		sb.append(" ] ");

		//JsonFunctions.processFieldName(sb, "minimum_should_match");
		//JsonFunctions.processFieldValue(sb, minimumShouldMatch);
		sb.append(" } ");
	}
}
