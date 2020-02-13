package com.progbits.api.elastic.query;

/**
 *
 * @author scarr
 */
public interface Query {

	public String toJson();

	public void toJson(StringBuilder sb);
}
