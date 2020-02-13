package com.progbits.api.elastic.query;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scarr
 */
public class BoolQuery implements Query {

	private List<Query> must = new ArrayList<>();
	private List<Query> mustNot = new ArrayList<>();
	private List<Query> should = new ArrayList<>();

	public List<Query> getMust() {
		return must;
	}

	public void setMust(List<Query> must) {
		this.must = must;
	}

	public List<Query> getMustNot() {
		return mustNot;
	}

	public void setMustNot(List<Query> mustNot) {
		this.mustNot = mustNot;
	}

	public List<Query> getShould() {
		return should;
	}

	public void setShould(List<Query> should) {
		this.should = should;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "bool");

		sb.append(" { ");

		boolean bFirst = true;

		if (must != null && must.size() > 0) {
			JsonFunctions.processFieldName(sb, "must");
			sb.append(" [ ");

			boolean bFirstLoop = true;

			for (Query q : must) {
				if (bFirstLoop) {
					bFirstLoop = false;
				} else {
					sb.append(", ");
				}

				sb.append(" { ");

				q.toJson(sb);

				sb.append(" } ");
			}

			sb.append(" ]");

			bFirst = false;
		}

		if (mustNot != null && mustNot.size() > 0) {
			if (!bFirst) {
				sb.append(",");
			}

			JsonFunctions.processFieldName(sb, "must_not");
			sb.append(" [ ");

			boolean bFirstLoop = true;

			for (Query q : mustNot) {
				if (bFirstLoop) {
					bFirstLoop = false;
				} else {
					sb.append(", ");
				}

				sb.append(" { ");
				q.toJson(sb);
				sb.append(" } ");
			}

			sb.append(" ]");

			bFirst = false;
		}

		if (should != null && should.size() > 0) {
			if (!bFirst) {
				sb.append(",");
			}

			JsonFunctions.processFieldName(sb, "should");
			sb.append(" [ ");

			boolean bFirstLoop = true;

			for (Query q : should) {
				if (bFirstLoop) {
					bFirstLoop = false;
				} else {
					sb.append(", ");
				}

				sb.append(" { ");
				q.toJson(sb);
				sb.append(" } ");
			}

			sb.append(" ]");

			bFirst = false;
		}

		sb.append(" } ");

	}

}
