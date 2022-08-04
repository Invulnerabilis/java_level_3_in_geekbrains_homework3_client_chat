package ru.vk.client_chat.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.vk.client_chat.StartClient;
import ru.vk.client_chat.models.Network;

public class SignController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField loginReg;

    @FXML
    private TextField passReg;

    @FXML
    private TextField usernameReg;
    private Network network;
    private StartClient startClient;

    @FXML
    void initialize() {

    }

    @FXML
    void singIn() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            System.out.println("Ошибка ввода при аутентификации");
            System.out.println();
            startClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не должны быть пустыми");
            return;
        }

        if (login.length() > 32 || password.length() > 32) {
            startClient.showErrorAlert("Ошибка ввода при аутентификации", "Длина логина и пароля должны быть не более 32 символов");
            return;
        }

        String authMessage = network.sendAuthMessage(login, password);

        if (authMessage == null) {
            startClient.openChatDialog();
        } else {
            startClient.showErrorAlert("Ошибка аутентификации", authMessage);
        }
    }

    @FXML
    void signUp() {
        String login = loginReg.getText().trim();
        String password = passReg.getText().trim();
        String username = usernameReg.getText().trim();

        if (login.length() == 0 || password.length() == 0 || username.length() == 0) {
            System.out.println("Ошибка ввода при регистрации");
            System.out.println();
            startClient.showErrorAlert("Ошибка ввода при регистрации", "Поля не должны быть пустыми");
            return;
        }

        if (login.length() > 32 || password.length() > 32 || username.length() > 32) {
            startClient.showErrorAlert("Ошибка ввода при регистрации", "Длина логина, пароля и имени должны быть не более 32 символов");
            return;
        }

        String registrationMessage = network.sendRegMessage(login, password, username);

        if (registrationMessage == null) {
            startClient.openChatDialog();
        } else {
            startClient.showErrorAlert("Ошибка аутентификации", registrationMessage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }
}
