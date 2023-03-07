package org.academic.Authentication;

import org.academic.Services.AuthDetails;
import org.academic.User.UserType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;

class AuthenticatorTest {

    @Test
    void authenticate() throws SQLException {
        MockedStatic<AuthDetails> authDetailsMockedStatic = org.mockito.Mockito.mockStatic(AuthDetails.class);
        String username = "staff";
        String password = "123s";


        authDetailsMockedStatic.when(() -> AuthDetails.verifyUser(username, password)).thenReturn(UserType.OFFICE_STAFF);
        authDetailsMockedStatic.when(() -> AuthDetails.verifyUser("invalid", "invalid")).thenReturn(null);

        boolean result = Authenticator.authenticate(username, password);
        boolean result2 = Authenticator.authenticate("invalid", "invalid");

        assertAll(
                () -> assertTrue(result),
                () -> assertFalse(result2)
        );

        authDetailsMockedStatic.verify(() -> AuthDetails.verifyUser(username, password));
        authDetailsMockedStatic.close();


    }

    @Test
    void logout() {
    }
}