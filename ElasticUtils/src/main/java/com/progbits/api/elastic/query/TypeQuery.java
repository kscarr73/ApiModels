package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class TypeQuery implements Query {

	private String _type = null;

	public TypeQuery() {
	}

	public TypeQuery(String type) {
		this._type = type;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "type");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, "value");

		JsonFunctions.processFieldValue(sb, _type);

		sb.append(" } ");
	}

}
