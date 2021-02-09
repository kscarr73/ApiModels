package com.progbits.api.auth.db;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.progbits.api.auth.Authenticate;
import com.progbits.api.model.ApiObject;
import com.progbits.db.SsDbUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author scarr
 */
@Component(name = "AuthenticateDb", property = {"name=AuthenticateDb"})
public class AuthenticateDb implements Authenticate {

	private DataSource dataSource;

	private static final String SQL_USER_LOGIN = "SELECT id, firstName, lastName, emailAddress, companyRole FROM sm_users WHERE company=? AND emailAddress=? AND password=?";
	private static final String SQL_USER_EMAILVALIDATION = "SELECT id FROM sm_users WHERE company=? AND emailAddress=?";
	private static final String SQL_ACTIVE_LOGINS = "SELECT id, userId, createdDate, bearerToken FROM sm_logins WHERE userId=? AND status=1 ORDER BY createdDate";
	private static final String SQL_INSERT_LOGINS = "INSERT INTO sm_logins (userId, bearerToken, createdDate, status) VALUES (?,?,?,?)";
	private static final String SQL_INSERT_USERS = "INSERT INTO sm_users (company,emailAddress,password,firstName,"
			+ "lastName,phoneNumber,companyRole,termsAndConditions) VALUES (?,?,?,?,?,?,?,?)";

	Algorithm algorithm = Algorithm.HMAC256("somethingLong");
	JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("apiweb").build();

	@Reference(name = "smartMapper")
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public ApiObject login(ApiObject subject) {
		ApiObject objRet = new ApiObject();

		List<Object> params = new ArrayList<>();
		params.add(subject.getString("company"));
		params.add(subject.getString("userName"));
		params.add(subject.getString("password"));

		try (Connection conn = dataSource.getConnection()) {
			List<Map<String, Object>> objRow = SsDbUtils.queryForAllRows(conn, SQL_USER_LOGIN, params.toArray());

			if (objRow.isEmpty()) {
				objRet.setInteger("status", 0);
				objRet.setString("message", "Username or Password Incorrect");
			} else {
				String strToken = checkExistingToken(conn, (Integer) objRow.get(0).get("id"));

				if (strToken == null) {
					strToken = generateAccessKey(objRow.get(0));

					insertLogin(conn, (Integer) objRow.get(0).get("id"), strToken);
				}

				objRet.put("emailAddress", objRow.get(0).get("emailAddress"));
				objRet.put("companyRole", objRow.get(0).get("companyRole"));
				objRet.put("firstName", objRow.get(0).get("firstName"));
				objRet.put("lastName", objRow.get(0).get("lastName"));
				objRet.put("company", objRow.get(0).get("company"));
				
				objRet.setString("access_key", strToken);
			}
		} catch (SQLException sqx) {
			objRet.setInteger("status", -1);
			objRet.setString("error", sqx.getMessage());
		} catch (Exception ex) {
			objRet.setInteger("status", -1);
			objRet.setString("error", ex.getMessage());
		}

		return objRet;
	}

	private String checkExistingToken(Connection conn, Integer userId) {
		String strRet = null;
		Object[] params = new Object[1];
		params[0] = userId;

		try {
			List<Map<String, Object>> objRow = SsDbUtils.queryForAllRows(conn, SQL_ACTIVE_LOGINS, params);

			if (!objRow.isEmpty()) {
				strRet = (String) objRow.get(0).get("bearerToken");
			}
		} catch (Exception ex) {

		}

		return strRet;
	}

	private void insertLogin(Connection conn, Integer userId, String bearerToken) {
		Date dt = new Date();

		Object[] params = new Object[4];
		params[0] = userId;
		params[1] = bearerToken;
		params[2] = dt;
		params[3] = 1;

		try {
			SsDbUtils.update(conn, SQL_INSERT_LOGINS, params);
		} catch (Exception ex) {

		}
	}

	private String generateAccessKey(Map<String, Object> userRow) {
		String retStr = null;

		try {
			OffsetDateTime dt = OffsetDateTime.now().plusHours(4);

			retStr = JWT.create().withIssuer("apiweb")
					.withKeyId(Integer.toString((Integer) userRow.get("id")))
					.withExpiresAt(new Date(dt.toInstant().toEpochMilli()))
					.sign(algorithm);
		} catch (JWTCreationException jce) {

		}

		return retStr;
	}

	@Override
	public ApiObject validateEmail(ApiObject subject) {
		ApiObject objRet = new ApiObject();

		List<Object> params = new ArrayList<>();
		params.add(subject.getString("company"));
		params.add(subject.getString("userName"));

		try (Connection conn = dataSource.getConnection()) {
			List<Map<String, Object>> objRow = SsDbUtils.queryForAllRows(conn, SQL_USER_EMAILVALIDATION, params.toArray());

			if (objRow.isEmpty()) {
				objRet.setInteger("status", 0);
			} else {
				objRet.setInteger("status", 1);
			}
		} catch (Exception ex) {
			objRet.setInteger("status", -1);
			objRet.setString("message", "Call Failed: " + ex.getMessage());
		}

		return objRet;
	}

	@Override
	public ApiObject verifyEmail(ApiObject subject) {
		ApiObject retObj = new ApiObject();
		
		// TODO:  Send Email with Response Token
		
		retObj.put("responseToken", 145613);
		
		return retObj;
	}

	@Override
	public ApiObject storeUser(ApiObject subject) {
		Date dt = new Date();

		ApiObject retEmailValidate = validateEmail(subject);

		switch (retEmailValidate.getInteger("status")) {
			case 0:
				List<Object> params = new ArrayList<>();

				params.add(subject.getString("company"));
				params.add(subject.getString("userName"));
				params.add(subject.getString("password"));
				params.add(subject.getString("firstName"));
				params.add(subject.getString("lastName"));
				params.add(subject.getString("phoneNumber"));
				params.add(subject.getString("companyRole"));
				params.add(subject.getLong("termsAndConditions"));

				try (Connection conn = dataSource.getConnection()) {
					Integer iVal = SsDbUtils.insertWithKey(conn, SQL_INSERT_USERS, params.toArray(), new String[]{"id"});

					subject.setInteger("id", iVal);
				} catch (Exception ex) {
					subject.setString("message", ex.getMessage());
				}
				break;
			case 1:
				subject.setString("message", "Email Address Already Exists");
				break;
			default:
				subject.setString("message", retEmailValidate.getString("message"));
				break;
		}

		return subject;
	}

	@Override
	public ApiObject validateToken(String token) {
		ApiObject retObj = new ApiObject();

		try {
			DecodedJWT retJwt = jwtVerifier.verify(token);
			retObj.setInteger("status", 1);
		} catch (JWTVerificationException ex) {
			retObj.setInteger("status", 0);
		}

		return retObj;
	}

}
