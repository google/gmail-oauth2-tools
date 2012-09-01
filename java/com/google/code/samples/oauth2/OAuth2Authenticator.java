/* Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.samples.oauth2;

import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.smtp.SMTPTransport;

import java.security.Provider;
import java.security.Security;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Session;
import javax.mail.URLName;


/**
 * Performs OAuth2 authentication.
 *
 * <p>Before using this class, you must call {@code initialize} to install the
 * OAuth2 SASL provider.
 */
public class OAuth2Authenticator {
  private static final Logger logger =
      Logger.getLogger(OAuth2Authenticator.class.getName());

  public static final class OAuth2Provider extends Provider {
    private static final long serialVersionUID = 1L;

    public OAuth2Provider() {
      super("Google OAuth2 Provider", 1.0,
            "Provides the XOAUTH2 SASL Mechanism");
      put("SaslClientFactory.XOAUTH2",
          "com.google.code.samples.oauth2.OAuth2SaslClientFactory");
    }
  }

  /**
   * Installs the OAuth2 SASL provider. This must be called exactly once before
   * calling other methods on this class.
   */
  public static void initialize() {
    Security.addProvider(new OAuth2Provider());
  }

  /**
   * Connects and authenticates to an IMAP server with OAuth2. You must have
   * called {@code initialize}.
   *
   * @param host Hostname of the imap server, for example {@code
   *     imap.googlemail.com}.
   * @param port Port of the imap server, for example 993.
   * @param userEmail Email address of the user to authenticate, for example
   *     {@code oauth@gmail.com}.
   * @param oauthToken The user's OAuth token.
   * @param debug Whether to enable debug logging on the IMAP connection.
   *
   * @return An authenticated IMAPStore that can be used for IMAP operations.
   */
  public static IMAPStore connectToImap(String host,
                                        int port,
                                        String userEmail,
                                        String oauthToken,
                                        boolean debug) throws Exception {
    Properties props = new Properties();
    props.put("mail.imaps.sasl.enable", "true");
    props.put("mail.imaps.sasl.mechanisms", "XOAUTH2");
    props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
    Session session = Session.getInstance(props);
    session.setDebug(debug);

    final URLName unusedUrlName = null;
    IMAPSSLStore store = new IMAPSSLStore(session, unusedUrlName);
    final String emptyPassword = "";
    store.connect(host, port, userEmail, emptyPassword);
    return store;
  }

  /**
   * Connects and authenticates to an SMTP server with OAuth2. You must have
   * called {@code initialize}.
   *
   * @param host Hostname of the smtp server, for example {@code
   *     smtp.googlemail.com}.
   * @param port Port of the smtp server, for example 587.
   * @param userEmail Email address of the user to authenticate, for example
   *     {@code oauth@gmail.com}.
   * @param oauthToken The user's OAuth token.
   * @param debug Whether to enable debug logging on the connection.
   *
   * @return An authenticated SMTPTransport that can be used for SMTP
   *     operations.
   */
  public static SMTPTransport connectToSmtp(String host,
                                            int port,
                                            String userEmail,
                                            String oauthToken,
                                            boolean debug) throws Exception {
    Properties props = new Properties();
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.sasl.enable", "true");
    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
    props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
    Session session = Session.getInstance(props);
    session.setDebug(debug);

    final URLName unusedUrlName = null;
    SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
    // If the password is non-null, SMTP tries to do AUTH LOGIN.
    final String emptyPassword = "";
    transport.connect(host, port, userEmail, emptyPassword);

    return transport;
  }

  /**
   * Authenticates to IMAP with parameters passed in on the commandline.
   */
  public static void main(String args[]) throws Exception {
    if (args.length != 2) {
      System.err.println(
          "Usage: OAuth2Authenticator <email> <oauthToken>");
      return;
    }
    String email = args[0];
    String oauthToken = args[1];

    initialize();

    IMAPStore imapStore = connectToImap("imap.gmail.com",
                                        993,
                                        email,
                                        oauthToken,
                                        true);
    System.out.println("Successfully authenticated to IMAP.\n");
    SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com",
                                                587,
                                                email,
                                                oauthToken,
                                                true);
    System.out.println("Successfully authenticated to SMTP.");
  }
}
