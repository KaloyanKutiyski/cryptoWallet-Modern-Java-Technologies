package bg.sofia.uni.fmi.mjt.crypto.server.users;

import bg.sofia.uni.fmi.mjt.crypto.server.wallet.CryptoCurrencyWallet;

import java.util.Objects;

public class User {
    private String username;
    private String password;
    private CryptoCurrencyWallet wallet;

    public User(String username, String password, CryptoCurrencyWallet wallet) {
        this.username = username;
        this.password = password;
        this.wallet = wallet;
    }

    public String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    public CryptoCurrencyWallet getWallet() {
        return wallet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) && password.equals(user.password) && wallet.equals(user.wallet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, wallet);
    }
}
