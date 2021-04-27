package com.progbits.api.sql;

import com.progbits.api.model.ApiObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.OffsetDateTime;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestApiSqlUtils {

	@Test
	public void runTest() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/smartmapper?user=smartmapperUser&password=SMpwUFts1978")) {
			ApiObject objApi = ApiSqlUtils.querySql(conn, "SELECT * FROM sm_logins WHERE userId=?", new Object[]{1});

			assert objApi != null;
			assert objApi.getList("root").get(0).get("createdDate") instanceof OffsetDateTime;
			
			System.out.println(objApi.getDateTime("root[0].createdDate").toString());
		}
	}
}
