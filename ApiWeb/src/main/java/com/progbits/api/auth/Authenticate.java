package com.progbits.api.auth;

import com.progbits.api.model.ApiObject;

/**
 *
 * @author scarr
 */
public interface Authenticate {
	ApiObject login(ApiObject subject);
	ApiObject validateEmail(ApiObject subject);
	ApiObject storeUser(ApiObject subject);
	ApiObject validateToken(String token);
	ApiObject verifyEmail(ApiObject subject);
}
