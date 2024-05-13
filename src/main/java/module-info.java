module cn.reddragon.eportal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    //requires java.b

    requires org.controlsfx.controls;

    opens cn.reddragon.eportal to javafx.fxml;
    exports cn.reddragon.eportal;
    exports cn.reddragon.eportal.controller;
    opens cn.reddragon.eportal.controller to javafx.fxml;
    exports cn.reddragon.eportal.utils;
    opens cn.reddragon.eportal.utils to javafx.fxml;
}