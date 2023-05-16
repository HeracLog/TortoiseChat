package main.java.com.tortoise.chat.tortoisechat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField ServerPortField;
    @FXML
    private TextField ServerIPField;
    @FXML
    private TextField UsernameField;
    @FXML
    private Button JoinButton;

    static String IP;
    static int PORT;
    static String Username;




    @FXML
    protected void onJoinButtonClick(ActionEvent event) throws IOException {
        IP = ServerIPField.getText().trim();
        PORT = Integer.parseInt(ServerPortField.getText());
        Username = UsernameField.getText().trim();




        Scene ChatRoom = new Scene(FXMLLoader.load(getClass().getResource("ChatPanel.fxml")), 600,400);
        Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        currentStage.setScene(ChatRoom);
        currentStage.setTitle("Tortoise Chat");
        currentStage.show();

    }

}