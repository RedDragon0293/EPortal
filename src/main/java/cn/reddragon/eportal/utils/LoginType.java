package cn.reddragon.eportal.utils;

public enum LoginType {
    WAN("校园外网服务(out-campus NET)", "校园网"),
    CHINAMOBILE("中国移动(CMCC NET)", "中国移动"),
    CHINATELECOM("中国电信(常州)", "中国电信(常州)"),
    CHINAUNICOM("中国联通(常州)", "中国联通(常州)");
    public final String authName, displayName;

    LoginType(String authName, String displayName) {
        this.authName = authName;
        this.displayName = displayName;
    }
}
