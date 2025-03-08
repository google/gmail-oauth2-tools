package com.google.code.samples.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.jupiter.api.Test;

import com.google.common.annotations.VisibleForTesting;

import com.google.code.samples.oauth2.OAuth2SaslClientFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import com.google.code.samples.oauth2.OAuth2SaslClient;

public class OAuth2SaslClientFactoryTests {

    private final OAuth2SaslClientFactory oAuth2SaslClientFactory = new OAuth2SaslClientFactory();

    @BeforeAll
    static void initAll() {
        
    }

    @Test
    void getMechanismNameXOAUTH()
    {
        OAuth2SaslClient oAuth2SaslClient = new OAuth2SaslClient();
        assertEquals(oAuth2SaslClient.getMechanismName(), oAuth2SaslClientFactory.getMechanismNames(null)[0]);
    }
}