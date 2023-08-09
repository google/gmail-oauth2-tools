package main.pro.freemania.code.samples.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.jupiter.api.Test;

import main.com.google.code.samples.oauth2.OAuth2SaslClientFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
public class OAuth2SaslClientFactoryTests {

    private final OAuth2SaslClientFactory oAuth2SaslClientFactory = new OAuth2SaslClientFactory();

    @DisplayName("Assumptions needed for tests")
    @Test
    void trueAssumption() {
        assumeTrue((4 % 2) == 0);
        assertEquals(8 % 4, 0);
        assumeTrue((7 % 3) == 1);
        assertEquals(7 % 2, 1);
        assumeTrue((2 + 2) == 4);
        assertEquals(2 + 2, 4);
        assumeTrue((69 % 10) == 9);
        assumeTrue((430 / 10) == 43);
    }

    @BeforeAll
    static void initAll() {
        
    }
}