package ru.vk.client_chat.models;

import javafx.application.Platform;
import ru.vk.client_chat.StartClient;
import ru.vk.client_chat.controllers.ChatController;
import ru.vk.client_chat.enumeration.ClientCommands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Objects;

public class Network {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8888;
    //ВНИМАНИЕ! ПЕРЕЗАГРУЗКА СЕРВЕРА ЧЕРЕЗ 15 МИНУТ
    private final String host;
    private final int port;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;


    private Thread readMessage;

    private String username;
    private StartClient startClient;

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


        } catch (IOException e) {
            e.printStackTrace();
            startClient.showErrorAlert("Ошибка подключения", "Соединение не установлено");
        }
    }

    public void sendMessage(String message) {
        try {
            String[] parts = message.split("\\s+");
            if (socket == null) {
                throw new IOException();
            }
            if (message.equals(ClientCommands.END_CLIENT_CMD_PREFIX.getCommand())) {
                out.writeUTF(String.format("%s", ClientCommands.END_CLIENT_CMD_PREFIX.getCommand()));
                disconnect();
            } else if (parts[0].equals(ClientCommands.CHANGE_USERNAME.getCommand())) {
                out.writeUTF(String.format("%s", message));
            } else if (parts[0].equals(ClientCommands.CHANGE_PASSWORD.getCommand())) {
                out.writeUTF(String.format("%s", message));
            } else {
                out.writeUTF(String.format("%s %s", ClientCommands.CLIENT_MSG_CMD_PREFIX.getCommand(), message));
            }
        } catch (IOException e) {
            e.printStackTrace();
            startClient.showErrorAlert("Ошибка подключения", "Соединение не установлено");
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        try {
            if (socket == null) {
                throw new IOException();
            }
            out.writeUTF(String.format("%s %s %s", ClientCommands.PRIVATE_MSG_CMD_PREFIX.getCommand(), recipient, message));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке сообщения");
        }
    }

    public String sendAuthMessage(String login, String password) {
        try {
            out.writeUTF(String.format("%s %s %s", ClientCommands.AUTH_CMD_PREFIX.getCommand(), login, password));
            String response = in.readUTF();

            if (response.startsWith(ClientCommands.AUTHOK_CMD_PREFIX.getCommand())) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке сообщения");
            return e.getMessage();
        }
    }

    public String sendRegMessage(String login, String password, String username) {
        try {
            out.writeUTF(String.format("%s %s %s %s", ClientCommands.REG_CMD_PREFIX.getCommand(), login, password, username));
            String response = in.readUTF();

            if (response.startsWith(ClientCommands.REGOK_CMD_PREFIX.getCommand())) {
                this.username = response.split("\\s+", 2)[1];
                return null;
            } else {
                return response.split("\\s+", 2)[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при отправке сообщения");
            return e.getMessage();
        }
    }

    public void waitMessage(ChatController chatController) {
        readMessage = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();

                    String typeMessage = message.split("\\s+")[0];
                    if (!typeMessage.startsWith("/")) {
                        System.out.println("Неверный запрос");
                    }

                    ClientCommands command = null;
                    for (ClientCommands com :
                            ClientCommands.values()) {
                        if (typeMessage.equals(com.getCommand())) {
                            command = com;
                        }
                    }

                    switch (Objects.requireNonNull(command)) {
                        case CLIENT_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 3);
                            String sender = parts[1];
                            String messageFromSender = parts[2];

                            if (sender.equals(username)) {
                                sender = "Я";
                            }

                            String finalSender = sender;
                            Platform.runLater(() -> chatController.appendMessage(finalSender, messageFromSender));
                        }
                        case PRIVATE_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+", 3);
                            String sender = parts[1];
                            String messageFromSender = parts[2];

                            Platform.runLater(() -> chatController.appendMessage("[pm]" + sender, messageFromSender));
                        }
                        case SERVER_MSG_CMD_PREFIX -> {
                            String[] parts = message.split("\\s+");
                            if (parts[1].equals(ClientCommands.CONDITION_USERS.getCommand())) {
                                String[] data = new String[parts.length - 2];
                                for (int i = 0; i < parts.length - 2; i++) {
                                    data[i] = parts[i + 2];
                                }
                                chatController.updateUserList(data);
                            } else {
                                String serverMessage = getMessageWithoutCommand(parts);
                                chatController.appendServerMessage(serverMessage);
                            }
                        }
                        case DATA -> {
                            chatController.printData(message.substring(typeMessage.length()).replaceAll(username, "Я"));
                        }
                        default -> {
                            chatController.appendServerMessage("Неверный запрос");
                            System.out.println("Неверный запрос");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                startClient.showErrorAlert("Ошибка подключения", "Соединение не установлено");
            }
        });

        readMessage.setDaemon(true);
        readMessage.start();
    }

    public String getMessageWithoutCommand(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            sb.append(parts[i]).append(" ");
        }
        return sb.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setStartClient(StartClient startClient) {
        this.startClient = startClient;
    }

    public StartClient getStartClient() {
        return startClient;
    }

    public void disconnect() {
        readMessage.interrupt();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
