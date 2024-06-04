package cn.reddragon.eportal.account;

import java.util.ArrayList;

public class AccountManager {
    public static final ArrayList<Account> accounts = new ArrayList<>();

    public static void addAccount(String username, String password) {
        accounts.add(new Account(username, password));
    }
}
