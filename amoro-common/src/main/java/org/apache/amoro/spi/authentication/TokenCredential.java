package org.apache.amoro.spi.authentication;

import java.util.Map;

public interface TokenCredential {
	String token();

	Map<String, String> extraInfo();
}