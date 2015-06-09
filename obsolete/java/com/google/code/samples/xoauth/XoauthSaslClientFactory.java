/* Copyright 2010 Google Inc.
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

package com.google.code.samples.xoauth;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

/**
 * A SaslClientFactory that returns instances of XoauthSaslClient.
 *
 * <p>Only the "XOAUTH" mechanism is supported. Only the "imaps" protocol is
 * supported. The {@code callbackHandler} is passed to the XoauthSaslClient.
 * Other parameters are ignored.
 */
public class XoauthSaslClientFactory implements SaslClientFactory {
  public static final String OAUTH_TOKEN_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.oauthToken";
  public static final String OAUTH_TOKEN_SECRET_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.oauthTokenSecret";
  public static final String CONSUMER_KEY_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.consumerKey";
  public static final String CONSUMER_SECRET_PROP =
      "mail.imaps.sasl.mechanisms.xoauth.consumerSecret";

  public SaslClient createSaslClient(String[] mechanisms,
                                     String authorizationId,
                                     String protocol,
                                     String serverName,
                                     Map<String, ?> props,
                                     CallbackHandler callbackHandler) {
    boolean matchedMechanism = false;
    for (int i = 0; i < mechanisms.length; ++i) {
      if ("XOAUTH".equalsIgnoreCase(mechanisms[i])) {
        matchedMechanism = true;
        break;
      }
    }
    if (!matchedMechanism) {
      return null;
    }
    XoauthProtocol xoauthProtocol = null;
    if ("imaps".equalsIgnoreCase(protocol)) {
      xoauthProtocol = XoauthProtocol.IMAP;
    }
    if ("smtp".equalsIgnoreCase(protocol)) {
      xoauthProtocol = XoauthProtocol.SMTP;
    }
    if (xoauthProtocol == null) {
      return null;
    }
    return new XoauthSaslClient(xoauthProtocol,
                                (String) props.get(OAUTH_TOKEN_PROP),
                                (String) props.get(OAUTH_TOKEN_SECRET_PROP),
                                (String) props.get(CONSUMER_KEY_PROP),
                                (String) props.get(CONSUMER_SECRET_PROP),
                                callbackHandler);
  }

  public String[] getMechanismNames(Map<String, ?> props) {
    return new String[] {"XOAUTH"};
  }
}
