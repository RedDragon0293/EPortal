module cn.reddragon.eportal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;
    //requires java.b

    requires org.controlsfx.controls;
    requires org.apache.logging.log4j;

    opens cn.reddragon.eportal to javafx.fxml;
    exports cn.reddragon.eportal;
    exports cn.reddragon.eportal.controllers;
    opens cn.reddragon.eportal.controllers to javafx.fxml;
    exports cn.reddragon.eportal.utils;
    opens cn.reddragon.eportal.utils to javafx.fxml;
    exports cn.reddragon.eportal.config;
    opens cn.reddragon.eportal.config to javafx.fxml;
}