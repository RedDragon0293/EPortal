package cn.reddragon.eportal.account;

import java.util.ArrayList;

public class AccountManager {
    public static final ArrayList<Account> accounts = new ArrayList<>();

    public static void addAccount(String username, String password) {
        accounts.removeIf(it -> it.name().equals(username));
        accounts.add(new Account(username, password));
    }

    public static void deleteAccount(String username) {
        accounts.removeIf(it -> it.name().equals(username));
    }

    public static boolean contains(String username) {
        for (Account it : accounts) {
            if (it.name().equals(username))
                return true;
        }
        return false;
    }

    public static Account getAccount(String username) {
        for (Account it : accounts) {
            if (it.name().equals(username))
                return it;
        }
        return null;
    }
}
