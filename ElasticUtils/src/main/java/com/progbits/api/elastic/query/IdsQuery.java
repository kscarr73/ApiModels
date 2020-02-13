package com.progbits.api.elastic.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author scarr
 */
public class IdsQuery implements Query {

	private List<String> _ids = new ArrayList<>();

	public IdsQuery() {
	}

	public IdsQuery(String[] ids) {
		_ids = Arrays.asList(ids);
	}

	public List<String> getFieldValue() {
		return _ids;
	}

	public void setFieldValue(List<String> ids) {
		_ids = ids;
	}

	@Override
	public String toJson() {
		StringBuilder sb = new StringBuilder();

		toJson(sb);

		return sb.toString();
	}

	@Override
	public void toJson(StringBuilder sb) {
		JsonFunctions.processFieldName(sb, "ids");

		sb.append(" { ");

		JsonFunctions.processFieldName(sb, "values");

		sb.append(" [ ");

		int iCnt = 0;

		for (String id : _ids) {

			if (iCnt > 0) {
				sb.append(",");
			}

			sb.append("\"");
			sb.append(id);
			sb.append("\"");

			iCnt = iCnt + 1;
		}

		sb.append(" ] ");
		sb.append(" } ");
	}
}
