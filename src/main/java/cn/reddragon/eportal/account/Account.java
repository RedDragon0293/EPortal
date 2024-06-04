package cn.reddragon.eportal.account;

public final class Account {
    private final String name;
    private final String password;

    public Account(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

}
