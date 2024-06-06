package cn.reddragon.eportal;

import cn.reddragon.eportal.window.MainWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static Logger logger = LogManager.getLogger("EPortal");

    public static void main(String[] args) {
        Thread.currentThread().setName("EPortal Main");
        MainWindow.main(args);
    }
}
