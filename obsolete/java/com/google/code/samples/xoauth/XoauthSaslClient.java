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

import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;


/**
 * An XOAUTH implementation of SaslClient.
 */
class XoauthSaslClient implements SaslClient {
  private boolean isComplete = false;
  private final XoauthProtocol protocol;
  private final String oauthToken;
  private final String oauthTokenSecret;
  private final OAuthConsumer consumer;
  private final CallbackHandler callbackHandler;

  /**
   * Creates a new instance of the XoauthSaslClient. This will ordinarily only
   * be called from XoauthSaslClientFactory.
   */
  public XoauthSaslClient(XoauthProtocol protocol,
                          String oauthToken,
                          String oauthTokenSecret,
                          String consumerKey,
                          String consumerSecret,
                          CallbackHandler callbackHandler) {
    this.protocol = protocol;
    this.oauthToken = oauthToken;
    this.oauthTokenSecret = oauthTokenSecret;
    this.consumer = new OAuthConsumer(null, consumerKey, consumerSecret, null);
    this.callbackHandler = callbackHandler;
  }

  public String getMechanismName() {
    return "XOAUTH";
  }

  public boolean hasInitialResponse() {
    return true;
  }

  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (challenge.length > 0) {
      throw new SaslException("Unexpected server challenge");
    }

    NameCallback nameCallback = new NameCallback("Enter name");
    Callback[] callbacks = new Callback[] { nameCallback };
    try {
      callbackHandler.handle(callbacks);
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("Unsupported callback: " + e);
    } catch (IOException e) {
      throw new SaslException("Failed to execute callback: " + e);
    }
    String email = nameCallback.getName();

    XoauthSaslResponseBuilder responseBuilder = new XoauthSaslResponseBuilder();
    Exception caughtException = null;
    try {
      byte[] rv = responseBuilder.buildResponse(email,
                                                protocol,
                                                oauthToken,
                                                oauthTokenSecret,
                                                consumer);
      isComplete = true;
      return rv;
    } catch (IOException e) {
      caughtException = e;
    } catch (OAuthException e) {
      caughtException = e;
    } catch (URISyntaxException e) {
      caughtException = e;
    }
    throw new SaslException("Threw an exception building XOAUTH string: " +
                            caughtException);
  }

  public boolean isComplete() {
    return isComplete;
  }

  public byte[] unwrap(byte[] incoming, int offset, int len)
      throws SaslException {
    throw new IllegalStateException();
  }

  public byte[] wrap(byte[] outgoing, int offset, int len)
      throws SaslException {
    throw new IllegalStateException();
  }

  public Object getNegotiatedProperty(String propName) {
    if (!isComplete) {
      throw new IllegalStateException();
    }
    return null;
  }

  public void dispose() throws SaslException {
  }
}
