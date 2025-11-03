package com.simplemessenger.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private TextField ipField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // optional: placeholder text already in FXML
        statusLabel.setText("");
    }

    @FXML
    private void login(ActionEvent event) {
        String username = usernameField.getText().trim();
        String serverIP = ipField.getText().trim();

        if (username.isEmpty()) {
            statusLabel.setText("Please enter a username.");
            return;
        }
        if (serverIP.isEmpty()) {
            statusLabel.setText("Please enter the server IP.");
            return;
        }

        try {
            // Load chat UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simplemessenger/client/chat.fxml"));
            Parent chatRoot = loader.load();

            // Initialize chat controller
            ChatController controller = loader.getController();
            controller.initData(username, serverIP);

            // Switch scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(chatRoot));
            stage.setTitle("Simple Messenger - " + username);

            // re-add icon
            try {
                stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/simplemessenger/client/comment.png")));
            } catch (Exception ignore) {}

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading chat window.");
        }
    }
}
