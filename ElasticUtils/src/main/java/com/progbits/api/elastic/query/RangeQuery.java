package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class RangeQuery implements Query {

	private String fieldName;

	private Object greaterThanEqual = null;
	private Object greaterThan = null;
	private Object lessThanEqual = null;
	private Object lessThan = null;

	public RangeQuery() {
	}

	public RangeQuery(String fieldName, Object greaterThanEqual,
			Object greaterThan, Object lessThanEqual, Object lessThan) {
		this.fieldName = fieldName;
		this.greaterThanEqual = greaterThanEqual;
		this.greaterThan = greaterThan;
		this.lessThanEqual = lessThanEqual;
		this.lessThan = lessThan;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Object getGreaterThanEqual() {
		return greaterThanEqual;
	}

	public void setGreaterThanEqual(Object greaterThanEqual) {
		this.greaterThanEqual = greaterThanEqual;
	}

	public Object getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(Object greaterThan) {
		this.greaterThan = greaterThan;
	}

	public Object getLessThanEqual() {
		return lessThanEqual;
	}

	public void setLessThanEqual(Object lessThanEqual) {
		this.lessThanEqual = lessThanEqual;
	}

	public Object getLessThan() {
		return lessThan;
	}

	public void setLessThan(Object lessThan) {
		this.lessThan = lessThan;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "range");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, fieldName);

		sb.append(" { ");

		boolean bFirstField = true;

		if (greaterThanEqual != null) {
			if (bFirstField) {
				bFirstField = false;
			} else {
				sb.append(", ");
			}

			JsonFunctions.processFieldName(sb, "gte");
			JsonFunctions.processFieldValue(sb, greaterThanEqual);
		}

		if (greaterThan != null) {
			if (bFirstField) {
				bFirstField = false;
			} else {
				sb.append(", ");
			}

			JsonFunctions.processFieldName(sb, "gt");
			JsonFunctions.processFieldValue(sb, greaterThan);
		}

		if (lessThanEqual != null) {
			if (bFirstField) {
				bFirstField = false;
			} else {
				sb.append(", ");
			}

			JsonFunctions.processFieldName(sb, "lte");
			JsonFunctions.processFieldValue(sb, lessThanEqual);
		}

		if (lessThan != null) {
			if (bFirstField) {
				bFirstField = false;
			} else {
				sb.append(", ");
			}

			JsonFunctions.processFieldName(sb, "lt");
			JsonFunctions.processFieldValue(sb, lessThan);
		}

		sb.append(" } } ");
	}

}
