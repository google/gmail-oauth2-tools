package main.com.google.code.samples.oauth2;

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

import main.com.google.code.samples.oauth2.OAuth2SaslClient;

class OAuth2SaslClientTests {

    private final OAuth2SaslClient oAuth2SaslClient = new OAuth2SaslClient();

    @DisplayName("Assumptions needed for OAuth2SasClient tests")
    @Test
    void trueAssumption() {
        assumeTrue((10 % 4) == 2);
        assertEquals(8 % 3, 2);
        assumeTrue((12 % 3) == 0);
        assertEquals(21 % 4, 1);
        assumeTrue((5 + 9) == 14);
        assertEquals(((42 % 10) + 2), 4);
    }

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
}