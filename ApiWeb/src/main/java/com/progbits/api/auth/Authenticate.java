package com.progbits.api.auth;

import com.progbits.api.model.ApiObject;
import com.progbits.api.srv.ApplicationException;

/**
 *
 * @author scarr
 */
public interface Authenticate {
	ApiObject login(ApiObject subject);
	ApiObject logout(ApiObject subject);
	ApiObject validateEmail(ApiObject subject);
	ApiObject storeUser(ApiObject subject);
	ApiObject validateToken(String token, boolean throwError) throws ApplicationException;
	ApiObject verifyEmail(ApiObject subject);
	void updateUserEmailValidate(ApiObject obj);
}
