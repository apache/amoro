/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.authentication;

import org.apache.amoro.authentication.BasicPrincipal;
import org.apache.amoro.authentication.PasswdAuthenticationProvider;
import org.apache.amoro.authentication.PasswordCredential;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.utils.PreconditionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import java.text.MessageFormat;
import java.util.Hashtable;

public class LdapPasswdAuthenticationProvider implements PasswdAuthenticationProvider {
  public static final Logger LOG = LoggerFactory.getLogger(LdapPasswdAuthenticationProvider.class);

  private String ldapUrl;
  private String ldapUserParttern;
  private MessageFormat formatter;

  public LdapPasswdAuthenticationProvider(Configurations conf) {
    this.ldapUrl = conf.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL);
    PreconditionUtils.checkNotNullOrEmpty(
        ldapUrl, AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_URL.key());
    this.ldapUserParttern = conf.get(AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_USER_PATTERN);
    PreconditionUtils.checkNotNullOrEmpty(
        ldapUserParttern, AmoroManagementConf.HTTP_SERVER_LOGIN_AUTH_LDAP_USER_PATTERN.key());
    this.formatter = new MessageFormat(ldapUserParttern);
  }

  @Override
  public BasicPrincipal authenticate(PasswordCredential credential) throws SignatureCheckException {
    Hashtable<String, Object> env = new Hashtable<String, Object>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, ldapUrl);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_CREDENTIALS, credential.password());
    env.put(Context.SECURITY_PRINCIPAL, formatter.format(new String[] {credential.username()}));

    InitialDirContext initialLdapContext = null;
    try {
      initialLdapContext = new InitialDirContext(env);
    } catch (NamingException e) {
      throw new SignatureCheckException("Failed to authenticate via ldap authentication", e);
    } finally {
      if (initialLdapContext != null) {
        try {
          initialLdapContext.close();
        } catch (NamingException e) {
          LOG.error("Exception in closing {}", initialLdapContext, e);
        }
      }
    }
    return new BasicPrincipal(credential.username());
  }
}
