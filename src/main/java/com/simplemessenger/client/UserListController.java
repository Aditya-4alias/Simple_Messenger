package com.simplemessenger.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

public class UserListController {

    @FXML private ListView<String> userList;

    private String username;
    private String serverIP;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void initData(String username, String ip) {
        this.username = username;
        this.serverIP = ip;
        startClient();
        userList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) openChatWith(userList.getSelectionModel().getSelectedItem());
        });
    }

    private void startClient() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIP, 5000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                writer.write(username);
                writer.newLine();
                writer.flush();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[Users]")) {
                        updateUserList(line.substring(7).trim());
                    }
                }
            } catch (IOException ignored) {}
        }).start();
    }

    private void updateUserList(String list) {
        Platform.runLater(() -> {
            userList.getItems().clear();
            if (!list.isBlank()) userList.getItems().addAll(list.split(" "));
        });
    }

    private void openChatWith(String targetUser) {
        if (targetUser == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simplemessenger/client/chatwindow.fxml"));
            Scene scene = new Scene(loader.load());
            ChatWindowController ctrl = loader.getController();
            ctrl.initChat(username, serverIP, targetUser);

            Stage chatStage = new Stage();
            chatStage.setScene(scene);
            chatStage.setTitle("Chat - " + targetUser);
            chatStage.getIcons().add(new javafx.scene.image.Image(getClass().getResource("/com/simplemessenger/client/comment.png").toString()));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
