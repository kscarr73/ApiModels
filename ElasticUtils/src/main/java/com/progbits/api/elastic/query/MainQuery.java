package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public class MainQuery implements Query {

	private Query query = new MatchAllQuery();

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "query");

		sb.append(" { ");

		sb.append(query.toJson());

		sb.append(" }");

	}

}
