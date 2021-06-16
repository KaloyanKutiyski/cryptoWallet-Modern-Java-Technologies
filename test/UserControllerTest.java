import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.users.UsersController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class UserControllerTest {
    private static final String USERNAME1 = "username1";
    private static final String USERNAME2 = "username2";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final Object CLIENT_TOKEN1 = "ClientToken1";
    private static final Object CLIENT_TOKEN2 = "ClientToken2";

    private static final String INVALID_NAME_OR_PASSWORD
            = "[ Invalid username/password combination ]";

    private UsersController usersController;

    @Before
    public void before() {
        CoinApiClient coinApiClient = mock(CoinApiClient.class);
        usersController = new UsersController(coinApiClient);
    }

    @Test
    public void registerUserTest() {
        assertEquals(
                "[ Username " + USERNAME1 + " successfully registered ]",
                usersController.register(USERNAME1, PASSWORD));
        assertEquals(
                "[ Username " + USERNAME2 + " successfully registered ]",
                usersController.register(USERNAME2, PASSWORD));
    }

    @Test
    public void attemptToRegisterSameUserTwiceTest() {
        assertEquals(
                "[ Username " + USERNAME1 + " successfully registered ]",
                usersController.register(USERNAME1, PASSWORD));
        assertEquals(
                "[ Username " + USERNAME1 + " is already taken, select another one ]",
                usersController.register(USERNAME1, PASSWORD));
    }

    @Test
    public void loginTest() {
        usersController.register(USERNAME1, PASSWORD);
        assertEquals("[ User " + USERNAME1 + " successfully logged in ]",
                usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD));
    }

    @Test
    public void attemptToLoginWithWrongPasswordTest() {
        usersController.register(USERNAME1, PASSWORD);
        assertEquals(INVALID_NAME_OR_PASSWORD,
                usersController.login(CLIENT_TOKEN1, USERNAME1, WRONG_PASSWORD));
    }

    @Test
    public void attemptToLoginAsNonExistentUserTest() {
        assertEquals(INVALID_NAME_OR_PASSWORD,
                usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD));
    }

    @Test
    public void logoutTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        assertEquals("[ Successfully logged out ]",
                usersController.logout(CLIENT_TOKEN1));
    }

    @Test
    public void attemptToLogoutWithoutBeingLoggedInTest() {
        assertEquals("[ You are not logged in ]",
                usersController.logout(CLIENT_TOKEN1));
    }

    @Test
    public void hasUserTest() {
        usersController.register(USERNAME1, PASSWORD);
        assertTrue(usersController.hasUser(USERNAME1));
    }

    @Test
    public void doesNotHaveUserTest() {
        assertFalse(usersController.hasUser(USERNAME1));
    }

    @Test
    public void isLoggedInTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        assertTrue(usersController.isLoggedIn(CLIENT_TOKEN1));
    }

    @Test
    public void isNotLoggedInTest() {
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        assertFalse(usersController.isLoggedIn(CLIENT_TOKEN1));
    }

    @Test
    public void isNotLoggedInAfterLoggingOutTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        usersController.logout(CLIENT_TOKEN1);
        assertFalse(usersController.isLoggedIn(CLIENT_TOKEN1));
    }

    @Test
    public void isNotLoggedInAfterDisconnectingTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        usersController.disconnect(CLIENT_TOKEN1);
        assertFalse(usersController.isLoggedIn(CLIENT_TOKEN1));
    }

    @Test
    public void userOfLoggedInClientTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        assertEquals(USERNAME1,
                usersController.userOf(CLIENT_TOKEN1).getUsername());
    }

    @Test
    public void userOfNotLoggedInClientTest() {
        usersController.register(USERNAME1, PASSWORD);
        usersController.login(CLIENT_TOKEN1, USERNAME1, PASSWORD);
        assertNull(usersController.userOf(CLIENT_TOKEN2));
    }
}