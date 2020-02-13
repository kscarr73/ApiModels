package com.progbits.api.elastic.aggs;

/**
 *
 * @author scarr
 */
public interface Aggregate {

	public String toJson();

	public void toJson(StringBuilder sb);

}
