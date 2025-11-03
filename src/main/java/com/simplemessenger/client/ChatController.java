package com.simplemessenger.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatController {
    @FXML private ListView<String> userList;
    @FXML private VBox chatArea;
    @FXML private TextField messageField;
    @FXML private Label chatTitle;

    private String username;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    // simple in-memory history per peer
    private final Map<String, List<String>> chatHistory = new ConcurrentHashMap<>();
    private String currentRecipient;

    public void initData(String username, String serverIP) {
        this.username = username;
        connectToServer(serverIP);
    }

    private void connectToServer(String serverIP) {
        try {
            socket = new Socket(serverIP, 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // send username as first line
            writer.write(username);
            writer.newLine();
            writer.flush();

            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("/users ")) {
                            String csv = line.substring(7);
                            updateUserList(csv);
                        } else if (line.startsWith("/msg ")) {
                            // server sends "/msg sender:message"
                            String payload = line.substring(5);
                            String[] parts = payload.split(":", 2);
                            if (parts.length == 2) {
                                String sender = parts[0];
                                String msg = parts[1];
                                chatHistory.computeIfAbsent(sender, k -> new ArrayList<>()).add(sender + ": " + msg);
                                if (sender.equals(currentRecipient)) {
                                    Platform.runLater(() -> addMessage(sender + ": " + msg, false));
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("Connection closed: " + ex.getMessage());
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserList(String usersCSV) {
        Platform.runLater(() -> {
            List<String> users = new ArrayList<>(Arrays.asList(usersCSV.split(",")));
            users.removeIf(u -> u.trim().isEmpty());
            users.remove(username); // hide self
            Collections.sort(users);
            userList.getItems().setAll(users);
        });
    }

    @FXML
    private void initialize() {
        // selection changes -> load chat
        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                currentRecipient = null;
                chatTitle.setText("Select a user to start chatting...");
                chatArea.getChildren().clear();
            } else {
                currentRecipient = newVal;
                chatTitle.setText("Chat - " + newVal);
                loadChatHistory(newVal);
            }
        });
    }

    private void loadChatHistory(String user) {
        chatArea.getChildren().clear();
        List<String> history = chatHistory.getOrDefault(user, Collections.emptyList());
        for (String m : history) {
            boolean isOwn = m.startsWith("You: ");
            addMessage(m, isOwn);
        }
    }

    private void addMessage(String text, boolean isOwn) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(isOwn ? "message-you" : "message-other");
        lbl.setWrapText(true);
        chatArea.getChildren().add(lbl);
    }

    public void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || currentRecipient == null) return;

        try {
            // /msg recipient:message
            writer.write("/msg " + currentRecipient + ":" + msg);
            writer.newLine();
            writer.flush();

            // append to local history & UI
            chatHistory.computeIfAbsent(currentRecipient, k -> new ArrayList<>()).add("You: " + msg);
            addMessage("You: " + msg, true);
            messageField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
