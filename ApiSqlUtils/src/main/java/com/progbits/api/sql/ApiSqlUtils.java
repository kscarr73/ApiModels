package com.progbits.api.sql;

import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.db.SsDbUtils;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scarr
 */
public class ApiSqlUtils {
	/**
	 * Executes a SQL Statement and Returns a Valid ApiObject
	 * 
	 * This is used
	 * @param conn Connection to run the SQL Against
	 * @param sql SQL Statement to run
	 * @param args Arguments to use in the SQL Statement
	 * @return ApiObject with the column names as field Names
	 * 
	 * @throws ApiException 
	 */
	public static ApiObject querySql(Connection conn, String sql, Object[] args) throws ApiException {
		ApiObject retObj = new ApiObject();
		
		try {
			List<Map<String, Object>> sqlRun = SsDbUtils.queryForAllRows(conn, sql, args);
			
			retObj.createList("root");
			
			sqlRun.forEach(obj -> { 
				ApiObject row = new ApiObject();
				
				obj.forEach((k, v) -> {
					row.put(k, convertObject(v));
				});
				
				retObj.getList("root").add(row);
			});
		} catch (Exception ex) {
			throw new ApiException(ex.getMessage());
		}
		
		return retObj;
	}
	
	public static Object convertObject(Object v) {
		if (v instanceof Timestamp) {
			Instant instant = Instant.ofEpochMilli(((Timestamp) v).getTime());
			return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
		} else {
			return v;
		}
	}
}
