package com.progbits.api.elastic.aggs;

import com.progbits.api.elastic.query.JsonFunctions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scarr
 */
public class Aggregates implements Aggregate {

	private List<Aggregate> _aggs = new ArrayList<>();

	public void addAggregate(Aggregate agg) {
		_aggs.add(agg);
	}

	public List<Aggregate> getAggregates() {
		return _aggs;
	}

	public void setAggregates(List<Aggregate> _aggs) {
		this._aggs = _aggs;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "aggs");

		sb.append(" { ");

		int iCnt = 0;

		for (Aggregate agg : _aggs) {
			if (iCnt > 0) {
				sb.append(",");
			}
			agg.toJson(sb);

			iCnt++;
		}

		sb.append(" } ");
	}

}
