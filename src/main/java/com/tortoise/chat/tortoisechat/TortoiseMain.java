package main.java.com.tortoise.chat.tortoisechat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TortoiseMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TortoiseMain.class.getResource("Tortoise.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 262, 400);
        stage.setTitle("Login Screen");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}