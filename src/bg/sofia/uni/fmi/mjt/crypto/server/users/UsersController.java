package bg.sofia.uni.fmi.mjt.crypto.server.users;

import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.network.Main;
import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersController implements Serializable {
    private Map<String, User> users;
    private transient final Map<Object, User> loggedInUsers;
    private transient final CoinApiClient coinApiClient;
    private transient final Logger logger;

    public UsersController(final CoinApiClient coinApiClient) {
        users = new HashMap<>();
        loggedInUsers = new HashMap<>();
        this.coinApiClient = coinApiClient;

        logger = Logger.getGlobal();
        try {
            FileHandler fh = new FileHandler(Main.logDestination, true);
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "file logger could not be initialized");
        }
    }

    public boolean hasUser(final String username) {
        return users.containsKey(username);
    }

    public boolean isLoggedIn(final Object userToken) {
        return loggedInUsers.containsKey(userToken);
    }

    public User userOf(final Object userToken) {
        if (loggedInUsers.containsKey(userToken)) {
            return loggedInUsers.get(userToken);
        }
        return null;
    }

    public String register(final String username,
                           final String password) {
        if (!users.containsKey(username)) {
            User newUser = new User(
                    username, password, new CryptoCurrencyWallet(coinApiClient));
            users.put(username, newUser);
            return "[ Username "
                    + newUser.getUsername()
                    + " successfully registered ]";
        }
        return "[ Username " + username + " is already taken, select another one ]";
    }

    public String login(Object key, final String username, final String password) {
        String res;
        if (users.containsKey(username)
                && users.get(username).getPassword().equals(password)) {

            logout(key);
            loggedInUsers.put(key, users.get(username));
            res = "[ User " + username + " successfully logged in ]";
        } else {
            res = "[ Invalid username/password combination ]";
        }
        return res;
    }

    public String logout(Object key) {
        if (loggedInUsers.containsKey(key)) {
            loggedInUsers.remove(key);
            return "[ Successfully logged out ]";
        }
        return "[ You are not logged in ]";
    }

    public String disconnect(Object key) {
        loggedInUsers.remove(key);
        return "[ Disconnected from server ]";
    }

}
