package com.progbits.api.auth.db;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.progbits.api.auth.Authenticate;
import com.progbits.api.model.ApiObject;
import com.progbits.api.srv.ApplicationException;
import com.progbits.db.SsDbUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Date;
import javax.sql.DataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "AuthenticateDb", property = {"name=AuthenticateDb"})
public class AuthenticateDb implements Authenticate {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticateDb.class);

	private DataSource dataSource;

	private static final String SQL_USER_LOGIN = "SELECT id, firstName, lastName, emailAddress, companyRole FROM sm_users WHERE company=:company AND emailAddress=:userName AND password=:password";
	private static final String SQL_USER_EMAILVALIDATION = "SELECT id FROM sm_users WHERE company=:company AND emailAddress=:emailAddress";
	private static final String SQL_ACTIVE_LOGINS = "SELECT id, userId, createdDate, bearerToken FROM sm_logins WHERE userId=? AND status=1 ORDER BY createdDate";
	private static final String SQL_INSERT_LOGINS = "INSERT INTO sm_logins (userId, bearerToken, createdDate, status) VALUES (:userId,:bearerToken,:createdDate,:status)";
	private static final String SQL_INSERT_USERS = "INSERT INTO sm_users (company,emailAddress,password,firstName,"
			+ "lastName,phoneNumber,companyRole,termsAndConditions) VALUES (:company,:emailAddress,:password,:firstName,"
			+ ":lastName,:phoneNumber,:companyRole,:termsAndConditions)";

	private static final String SQL_UPDATE_LOGOUT = "UPDATE sm_logins SET status=0 WHERE userId=:userId";

	Algorithm algorithm = Algorithm.HMAC256("somethingLong");
	JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer("apiweb").build();

	@Reference(name = "smartMapper")
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public ApiObject login(ApiObject subject) {
		ApiObject objRet = new ApiObject();

		try (Connection conn = dataSource.getConnection()) {
			ApiObject objSql = SsDbUtils.querySqlAsApiObject(conn, SQL_USER_LOGIN, subject);

			if (!objSql.isSet("root")) {
				objRet.setInteger("status", 0);
				objRet.setString("message", "Username or Password Incorrect");
			} else {
				ApiObject objRow = objSql.getObject("root[0]");

				String strToken = checkExistingToken(conn, objRow.getInteger("id"));

				if (strToken == null) {
					strToken = generateAccessKey(objRow);

					insertLogin(conn, objRow.getInteger("id"), strToken);
				} else {
					ApiObject valToken = validateToken(strToken);
					
					if (valToken.getInteger("status") == 0) {
						// Invalid Token so update and recreate
						strToken = generateAccessKey(objRow);

						insertLogin(conn, objRow.getInteger("id"), strToken);
					}
				}

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

	@Override
	public ApiObject logout(ApiObject subject) {
		ApiObject objRet = new ApiObject();

		try (Connection conn = dataSource.getConnection()) {
			SsDbUtils.updateObject(conn, SQL_UPDATE_LOGOUT, subject);

			objRet.setInteger("status", 1);
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
			ApiObject objRow = SsDbUtils.querySqlAsApiObject(conn, SQL_ACTIVE_LOGINS, params);

			if (objRow.isSet("root")) {
				strRet = objRow.getString("root[0].bearerToken");
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}

		return strRet;
	}

	private void insertLogin(Connection conn, Integer userId, String bearerToken) {
		OffsetDateTime dt = OffsetDateTime.now();

		ApiObject params = new ApiObject();

		params.setInteger("userId", userId);
		params.setString("bearerToken", bearerToken);
		params.setDateTime("createdDate", dt);
		params.setInteger("status", 1);

		try {
			SsDbUtils.updateObject(conn, SQL_UPDATE_LOGOUT, params);
			SsDbUtils.updateObjectWithCount(conn, SQL_INSERT_LOGINS, params);
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	private String generateAccessKey(ApiObject userRow) {
		String retStr = null;

		try {
			OffsetDateTime dt = OffsetDateTime.now().plusHours(4);

			retStr = JWT.create().withIssuer("apiweb")
					.withKeyId(Integer.toString(userRow.getInteger("id")))
					.withExpiresAt(new Date(dt.toInstant().toEpochMilli()))
					.sign(algorithm);
		} catch (JWTCreationException jce) {
			LOG.error(jce.getMessage(), jce);
		}

		return retStr;
	}

	@Override
	public ApiObject validateEmail(ApiObject subject) {
		ApiObject objRet = new ApiObject();

		try (Connection conn = dataSource.getConnection()) {
			if (subject.isSet("userName")) {
				subject.setString("emailAddress", subject.getString("userName"));
			}
			
			ApiObject objRow = SsDbUtils.querySqlAsApiObject(conn, SQL_USER_EMAILVALIDATION, subject);

			if (!objRow.isSet("root")) {
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

		subject.setInteger("status", retEmailValidate.getInteger("status"));

		switch (retEmailValidate.getInteger("status")) {
			case 0:
				try (Connection conn = dataSource.getConnection()) {
				Integer iVal = SsDbUtils.insertObjectWithKey(conn, SQL_INSERT_USERS, new String[]{"id"}, subject);

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

		if (subject.containsKey("password")) {
			subject.remove("password");
		}

		return subject;
	}

	@Override
	public ApiObject validateToken(String token) throws ApplicationException {
		ApiObject retObj = new ApiObject();

		if (token == null) {
			throw new ApplicationException(400, "AUTHORIZATION is REQUIRED");
		}
		try {
			String lclToken;

			if (token.startsWith("Bearer")) {
				lclToken = token.substring(7);
			} else {
				lclToken = token;
			}

			DecodedJWT retJwt = jwtVerifier.verify(lclToken);
			retObj.setInteger("id", Integer.valueOf(retJwt.getKeyId()));
			retObj.setInteger("status", 1);
		} catch (JWTVerificationException ex) {
			throw new ApplicationException(403, "Forbidden");
		}

		return retObj;
	}

}
