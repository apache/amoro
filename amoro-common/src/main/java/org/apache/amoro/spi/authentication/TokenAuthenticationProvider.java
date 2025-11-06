package org.apache.amoro.spi.authentication;

import org.apache.amoro.exception.SignatureCheckException;

import java.security.Principal;

public interface TokenAuthenticationProvider {
	/**
	 * TokenAuthenticationProvider is used by the Amoro server authentication layer to validate
	 * Bearer tokens, such as JWT (JSON Web Token), provided in client requests.
	 * If the token is invalid, expired, or fails signature verification, a
	 * {@link SignatureCheckException} should be thrown to deny access.
	 *
	 * @param credential The Bearer token credential (e.g., JWT) received in the connection request
	 * @return The {@link Principal} associated with the authenticated token
	 * @throws SignatureCheckException If the token is invalid, expired, or fails verification
	 */

	Principal authenticate(TokenCredential credential) throws SignatureCheckException;
}
