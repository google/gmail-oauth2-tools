package com.google.code.samples.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.maven.surefire.api.testset.TestSetFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import javax.security.auth.callback.CallbackHandler;
import java.lang.reflect.Field;
import com.google.code.samples.oauth2.OAuth2SaslClient;

class OAuth2SaslClientTests {

    private final OAuth2SaslClient oAuth2SaslClient = new OAuth2SaslClient();

    @Test
    public void oAuth2SaslClientgetNegotiatedPropertyThrows() throws TestSetFailedException {

        String message = new String("propertyToSet");

        assertThrows(IllegalStateException.class
        , () -> {
           oAuth2SaslClient.getNegotiatedProperty(message);
        });
    }

    @Test
    public void hasInitialResponseTest()
    {
        assertTrue(oAuth2SaslClient.hasInitialResponse());
    }

    public Field getPrivateFieldObject (String nameOfField) throws NoSuchFieldException
    {
        return OAuth2SaslClient.class.getDeclaredField(nameOfField);
    }

    @Test
    public void getNegotiatedPropertyMechanismName() throws IllegalAccessException, NoSuchFieldException
    {
        Field f = getPrivateFieldObject("MechanismName");
        f.setAccessible(true);
        String name = (String)f.get(this.oAuth2SaslClient);
        assertEquals(oAuth2SaslClient.getMechanismName(), name);
    }
}