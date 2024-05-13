package cn.reddragon.eportal.utils;

public enum LoginType {
    WAN("校园外网服务(out-campus NET)", "CampusNet"),
    CHINAMOBILE("中国移动(CMCC NET)", "ChinaMobile"),
    CHINATELECOM("中国电信(常州)", "ChinaTelecom"),
    CHINAUNICOM("中国联通(常州)", "ChinaUnicom");
    public final String authName, displayName;

    LoginType(String authName, String displayName) {
        this.authName = authName;
        this.displayName = displayName;
    }
}
