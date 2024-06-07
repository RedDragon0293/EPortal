package cn.reddragon.eportal.window;

import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.window.controllers.AccountController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AccountWindow {
    public static AccountController controller;
    public static Stage fatherStage;
    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    private static void init() throws IOException {
        FXMLLoader loader = new FXMLLoader(AccountWindow.class.getResource("account-view.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle("账号管理");
        stage.setScene(scene);
        stage.initOwner(fatherStage);
        stage.initModality(Modality.WINDOW_MODAL);
        ListView<String> listView = controller.listView;
        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            controller.onSelectionChanged(newValue);
        });
        TextField f1 = controller.usernameField;
        f1.textProperty().addListener((observableValue, oldValue, newValue) -> controller.onTextFieldChanged());
        TextField f2 = controller.passwordField;
        f2.textProperty().addListener(((observableValue, oldValue, newValue) -> controller.onTextFieldChanged()));
        ObservableList<String> items = listView.getItems();
        for (Account account : AccountManager.accounts) {
            items.add(account.name());
        }
        AccountWindow.stage = stage;
    }

    public static void open() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage.showAndWait();
    }
}
