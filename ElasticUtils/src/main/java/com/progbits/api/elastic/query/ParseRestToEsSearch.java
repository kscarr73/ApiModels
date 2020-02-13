package com.progbits.api.elastic.query;

import com.progbits.api.elastic.aggs.TermsAggregate;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * Parses a Rest based URL to create an EsSearch object.
 *
 *
 * @author scarr
 */
public class ParseRestToEsSearch {

	// Example Query String:  desc=*Mod*&sort(+desc)
	public static EsSearch parseRest(Map<String, String[]> params,
			Map<String, String> hdrs) {
		EsSearch search = new EsSearch();

		MainQuery mainQuery = new MainQuery();

		Integer iStart = 0;
		Integer iCount = 0;

		search.setQuery(mainQuery);

		String rangeHdr = hdrs.get("Range");

		if (rangeHdr != null) {
			String sRange = rangeHdr.substring(rangeHdr.indexOf("=") + 1);

			String[] lstRange = sRange.split("-");

			iStart = Integer.valueOf(lstRange[0]);

			if (lstRange.length > 1) {
				if (!lstRange[1].isEmpty()) {
					iCount = Integer.valueOf(lstRange[1]);
				} else {
					iCount = 0;
				}
			}
			// Set range on query

			search.setStart(iStart);
			search.setCount(iCount);
		}

		String sSortKey = null;

		// Do sort iteration
		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			if (entry.getKey().startsWith("sort")) {
				String sSort = entry.getKey().substring(5, entry.getKey().
						length() - 1);

				String[] splSort = sSort.split(",");

				for (String str : splSort) {
					search.addSortField(str);
				}

				sSortKey = entry.getKey();
			}
		}

		if (sSortKey != null) {
			params.remove(sSortKey);
		}

		BoolQuery bQuery = null;

		// Check for Query Parameter
		if (params.containsKey("query")) {
			String sQuery = null;

			if (params.get("query").length > 0) {
				sQuery = params.get("query")[0];
			}

			if (bQuery == null) {
				bQuery = new BoolQuery();
			}
			processQueryParam(sQuery, bQuery);
			params.remove("query");
		}

		if (!params.isEmpty()) {
			if (bQuery == null) {
				bQuery = new BoolQuery();
			}

			for (Map.Entry<String, String[]> entry : params.entrySet()) {
				if (entry.getValue() != null) {
					if (entry.getValue().length == 1) {
						String strValue = entry.getValue()[0];

						if (strValue.startsWith("*") && strValue.endsWith("*")) {
							strValue = strValue.substring(1,
									strValue.length() - 1);

							bQuery.getMust().add(new RegExpQuery(entry.getKey(),
									strValue + ".*"));
						} else if (strValue.startsWith("*")) {
							strValue = strValue.substring(1);

							bQuery.getMust().add(new RegExpQuery(entry.getKey(),
									".*" + strValue));
						} else if (strValue.endsWith("*")) {
							strValue = strValue.substring(0,
									strValue.length() - 1);

							bQuery.getMust().add(new RegExpQuery(entry.getKey(),
									strValue + ".*"));
						} else {
							bQuery.getMust().add(new TermQuery(entry.getKey(),
									returnObjectType("*", strValue)));
						}
					}
				}
			}
		}

		if (bQuery != null) {
			search.getQuery().setQuery(bQuery);
		}

		if ("true".equals(hdrs.get("fetchList"))) {
			search.getAggregates().addAggregate(new TermsAggregate(hdrs.get(
					"fetchField"), "terms", hdrs.get("fetchField"), null));
		}

		return search;
	}

	private static void processQueryParam(String strQuery, BoolQuery query) {
		// logicand|logicand|greaterEqual|column(\"@timestamp\")|value(date\"1430456400000\")||lessEqual|column(\"@timestamp\")|value(date\"1433048400000\")|||;

		String[] splQuery = strQuery.split("\\|");
		String curOp = null;
		String curNotOp = "";
		String curLogic = null;
		String curField = null;

		for (String sEntry : splQuery) {
			if (sEntry.startsWith("logic")) {
				curLogic = sEntry;
			} else if ("equal".equals(sEntry)) {
				curOp = sEntry;
			} else if ("greater".equals(sEntry)) {
				curOp = sEntry;
			} else if ("less".equals(sEntry)) {
				curOp = sEntry;
			} else if ("greaterEqual".equals(sEntry)) {
				curOp = sEntry;
			} else if ("lessEqual".equals(sEntry)) {
				curOp = sEntry;
			} else if ("match".equals(sEntry)) {
				curOp = sEntry;
			} else if ("contain".equals(sEntry)) {
				curOp = sEntry;
			} else if ("not".equals(sEntry)) {
				curNotOp = "not";
			} else if ("startWith".equals(sEntry)) {
				curOp = sEntry;
			} else if ("endWith".equals(sEntry)) {
				curOp = sEntry;
			} else if (sEntry.startsWith("column")) {
				// column("@timestamp")
				curField = sEntry.substring(8, sEntry.length() - 2);
			} else if (sEntry.startsWith("value")) {
				// value(date"1430456400000")

				String strTemp = sEntry.substring(6, sEntry.length() - 2);
				String[] sEntrySet = strTemp.split("\"", 2);

				String strType = sEntrySet[0];
				String strValue = sEntrySet[1];

				searchEntry(curLogic, curNotOp, curOp, curField, strType,
						strValue, query);

				curNotOp = "";
				curOp = "";
				curField = "";
			}
		}
	}

	private static void searchEntry(String logic, String not, String op,
			String field, String type, String value, BoolQuery query) {
		List<Query> tstQry = null;

		if ("logicand".equals(logic)) {
			if ("not".equals(not)) {
				tstQry = query.getMustNot();
			} else {
				tstQry = query.getMust();
			}
		} else {
			query.getShould();
		}

		if ("greater".equals(op) || "less".equals(op) || "greaterEqual".equals(
				op) || "lessEqual".equals(op)) {
			RangeQuery fldQry = null;

			for (Query fld : tstQry) {
				if (fld instanceof RangeQuery) {
					if (field.equals(((RangeQuery) fld).getFieldName())) {
						fldQry = (RangeQuery) fld;
						break;
					}
				}
			}

			if (fldQry == null) {
				fldQry = new RangeQuery(field, null, null, null, null);

				tstQry.add(fldQry);
			}

			setRangeQuery(fldQry, op, field, type, value);
		}

	}

	private static Object returnObjectType(String type, String value) {
		Object retObj = null;

		if ("string".equalsIgnoreCase(type)) {
			retObj = value;
		} else if ("number".equalsIgnoreCase(type)) {
			retObj = Long.parseLong(value);
		} else if ("decimal".equalsIgnoreCase(type)) {
			retObj = Double.parseDouble(value);
		} else if ("date".equalsIgnoreCase(type)) {
			retObj = new DateTime(Long.parseLong(value));
		} else if ("time".equalsIgnoreCase(type)) {
			retObj = new DateTime(Long.parseLong(value));
		} else if ("*".equals(type)) {
			// Here we try to guess the type
			boolean bFound = false;

			try {
				retObj = Long.parseLong(value);

				bFound = true;
			} catch (Exception ex) {

			}

			if (!bFound) {
				try {
					retObj = Double.parseDouble(value);

					bFound = true;
				} catch (Exception ex) {

				}
			}

			if (!bFound) {
				retObj = value;
			}
		}

		return retObj;
	}

	private static void setRangeQuery(RangeQuery fldQry, String op, String field,
			String type, String value) {
		if ("greater".equals(op)) {
			fldQry.setGreaterThan(returnObjectType(type, value));
		} else if ("less".equals(op)) {
			fldQry.setLessThan(returnObjectType(type, value));
		} else if ("greaterEqual".equals(op)) {
			fldQry.setGreaterThanEqual(returnObjectType(type, value));
		} else if ("lessEqual".equals(op)) {
			fldQry.setLessThanEqual(returnObjectType(type, value));
		}
	}
}
