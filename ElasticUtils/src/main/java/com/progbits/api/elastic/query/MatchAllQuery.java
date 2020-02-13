package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class MatchAllQuery implements Query {

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "match_all");

		sb.append(" { } ");
	}

}
