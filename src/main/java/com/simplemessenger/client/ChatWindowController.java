package com.simplemessenger.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.*;
import java.net.*;

public class ChatWindowController {

    @FXML private VBox chatArea;
    @FXML private TextField messageField;
    @FXML private Label chatTitle;

    private String username;
    private String targetUser;
    private String serverIP;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void initChat(String username, String ip, String targetUser) {
        this.username = username;
        this.serverIP = ip;
        this.targetUser = targetUser;
        chatTitle.setText("Chat with " + targetUser);
        connect();
    }

    private void connect() {
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
                    if (line.startsWith(targetUser + ":")) appendMessage(line);
                }
            } catch (IOException e) {
                appendMessage("[System] Disconnected.");
            }
        }).start();
    }

    @FXML
    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        try {
            writer.write("@" + targetUser + ": " + msg);
            writer.newLine();
            writer.flush();
            appendMessage(username + ": " + msg);
            messageField.clear();
        } catch (IOException e) {
            appendMessage("[Error] Message failed.");
        }
    }

    private void appendMessage(String text) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            label.getStyleClass().add(text.startsWith(username + ":") ? "message-you" : "message-other");
            chatArea.getChildren().add(label);
        });
    }
}
