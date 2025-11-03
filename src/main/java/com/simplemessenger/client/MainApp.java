package com.simplemessenger.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/simplemessenger/client/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simple Messenger - Login");

        // load icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/com/simplemessenger/client/comment.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
