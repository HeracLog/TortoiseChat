package com.tortoiseshell.tortoisechat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Server Screen");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("T(S)C_Icon.png")));
        stage.setScene(scene);
        stage.show();
    }


}