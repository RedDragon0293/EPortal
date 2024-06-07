package cn.reddragon.eportal.window.controllers;

import cn.reddragon.eportal.account.Account;
import cn.reddragon.eportal.account.AccountManager;
import cn.reddragon.eportal.window.AccountWindow;
import cn.reddragon.eportal.window.MainWindow;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;

import java.util.Optional;

public class AccountController {
    @FXML
    public ListView<String> listView;
    @FXML
    public Button editButton;
    @FXML
    public Button deleteButton;
    @FXML
    public Button addButton;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label resultText;

    public void onTextFieldChanged() {
        if (usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        } else {
            addButton.setDisable(false);
            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    public void onSelectionChanged(String newValue) {
        if (newValue == null) {
            usernameField.setText("");
            passwordField.setText("");
            return;
        }
        Account account = AccountManager.getAccount(newValue);
        if (account != null) {
            usernameField.setText(account.name());
            passwordField.setText(account.password());
        }
        addButton.setDisable(true);
        editButton.setDisable(newValue.isBlank());
        deleteButton.setDisable(newValue.isBlank());
    }

    public void onAddButtonClicked() {
        String username = usernameField.getText();
        if (AccountManager.contains(username)) {
            resultText.setText("账号已存在!");
            return;
        }
        AccountManager.addAccount(username, passwordField.getText());
        listView.getItems().add(username);
        listView.getSelectionModel().select(username);
        MainWindow.controller.accountSelector.getItems().add(username);
        resultText.setText("添加成功.");
    }

    public void onEditButtonClicked() {
        String username = usernameField.getText();
        if (!AccountManager.contains(username)) {
            resultText.setText("账号不存在!");
            return;
        }
        AccountManager.addAccount(username, passwordField.getText());
        listView.getSelectionModel().select(username);
        resultText.setText("修改成功.");
    }

    public void onDeleteButtonClicked() {
        String username = listView.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除账号 " + username + " 吗?");
        alert.initOwner(AccountWindow.getStage());
        alert.initModality(Modality.WINDOW_MODAL);
        ButtonType typeYes = new ButtonType("是");
        ButtonType typeNo = new ButtonType("否");
        alert.getButtonTypes().setAll(typeYes, typeNo);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == typeNo)
            return;
        if (!AccountManager.contains(username)) {
            resultText.setText("账号不存在!");
            return;
        }
        AccountManager.deleteAccount(username);
        listView.getItems().remove(username);
        MainWindow.controller.accountSelector.getItems().remove(username);
        resultText.setText("删除成功.");
    }
}
