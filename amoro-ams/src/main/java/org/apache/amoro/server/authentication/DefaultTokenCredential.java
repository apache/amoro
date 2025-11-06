package org.apache.amoro.server.authentication;

import org.apache.amoro.spi.authentication.TokenCredential;

import java.util.Map;

public class DefaultTokenCredential implements TokenCredential {
	private String token;
	private Map<String, String> extraInfo;

	public DefaultTokenCredential(String token, Map<String, String> extraInfo) {
		this.token = token;
		this.extraInfo = extraInfo;
	}

	@Override
	public String token() {
		return token;
	}

	@Override
	public Map<String, String> extraInfo() {
		return extraInfo;
	}
}
