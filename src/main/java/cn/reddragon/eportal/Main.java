package cn.reddragon.eportal;

import cn.reddragon.eportal.window.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static final String name = "EPortal";
    public static final String version = "2.1.0";
    public static Logger logger = LogManager.getLogger(name);

    public static void main(String[] args) {
        Thread.currentThread().setName(name + " Main");
        MainWindow.main(args);
    }
}
