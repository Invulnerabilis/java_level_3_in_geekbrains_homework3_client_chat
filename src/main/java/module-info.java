module ru.vk.client_chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.vk.client_chat to javafx.fxml;
    exports ru.vk.client_chat;
    exports ru.vk.client_chat.controllers;
    opens ru.vk.client_chat.controllers to javafx.fxml;
}