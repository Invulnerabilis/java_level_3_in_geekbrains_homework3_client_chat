package ru.vk.client_chat;

/*
Java. Уровень 3. Урок 3.
Домашнее задание 3. "Средства ввода-вывода".

1. Добавить в сетевой чат запись локальной истории в текстовый файл на клиенте.

2. После загрузки клиента показывать ему последние 100 строк чата.

P.S.: Это только "Client Chat" проекта сетевого чата третьего урока.
"Server Client" сетевого чата находится в репозитории java_level_3_in_geekbrains_g ветка homework3
*/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.vk.client_chat.controllers.ChatController;
import ru.vk.client_chat.controllers.SignController;
import ru.vk.client_chat.models.Network;

import java.io.IOException;

public class StartClient extends Application {

    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;
    private SignController signController;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        network = new Network();
        network.setStartClient(this);
        network.connect();

        openAuthDialog();
        createChatDialog();
    }

    private void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader(StartClient.class.getResource("auth-view.fxml"));
        authStage = new Stage();
        Scene scene = new Scene(authLoader.load(), 600, 400);

        authStage.setScene(scene);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setTitle("Chatroom");
        authStage.setY(140);
        authStage.setX(650);
        authStage.show();

        SignController signController = authLoader.getController();

        signController.setNetwork(network);
        signController.setStartClient(this);
    }

    private void createChatDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(StartClient.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        primaryStage.setScene(scene);
        primaryStage.setY(140);
        primaryStage.setX(650);

        chatController = fxmlLoader.getController();
        chatController.setNetwork(network);
        chatController.setStartClient(this);

    }

    public void openChatDialog() {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());

        network.waitMessage(chatController);
        chatController.setUsernameTitle(network.getUsername());
    }

    public void showErrorAlert(String title, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        alert.show();
    }

    public void showInfoAlert(String title, String infoMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(infoMessage);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }
}