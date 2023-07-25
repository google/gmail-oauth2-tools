package com.google.code.samples.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import javax.security.auth.callback.CallbackHandler;
import java.lang.reflect.Field;
import com.google.code.samples.oauth2.OAuth2SaslClient;
import org.junit.jupiter.api.BeforeEach;



/**
* The OAuth2SaslClientTest runs a set of assumptions on modular arithemetic then tests various portions of OAuth2SaslClient class
*
* @author  John H. Freeman
* @version 1.0
* @since   2023-25-07
*/
public class OAuth2SaslClientTest {

    private final OAuth2SaslClient oAuth2SaslClient = new OAuth2SaslClient();

    @Test
    public void oAuth2SaslClientgetNegotiatedPropertyThrows() {

        String message = new String("propertyToSet");

        assertThrows(UnsupportedOperationException.class
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

    @Test
    public void assertWrapThrowsIllegalStateException()
    {
        assertThrows(UnsupportedOperationException.class, () -> oAuth2SaslClient.wrap(null, 0, 0));
    }
}