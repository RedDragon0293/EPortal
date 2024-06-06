package cn.reddragon.eportal.account;

public record Account(String name, String password) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Account a))
            return false;
        return name.equals(a.name);
    }
}
