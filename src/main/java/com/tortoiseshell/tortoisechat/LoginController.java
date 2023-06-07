package com.tortoiseshell.tortoisechat;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController {
    @FXML
    private TextField ServerPortField,ServerIPField,UsernameField,createUser;
    @FXML
    private PasswordField password,createPass,confirmPass;
    @FXML
    private Button JoinButton,signup,confirmSignup;
    @FXML
    private Pane loginPane,signupPane,aniPane;
    @FXML
    private Label errorLabel,errorLabelSignup;
    

    static String IP;
    static int PORT;
    static String Username;
    static Socket clienSocket;
    int passHash,usrHash;
    TranslateTransition signUp_Login;
    TranslateTransition login_SignUp;


    public void initialize()
    {
        signUp_Login = new TranslateTransition();
        login_SignUp = new TranslateTransition();
        login_SignUp.setNode(aniPane);
        login_SignUp.setByY(400);
        login_SignUp.setDuration(Duration.millis(400));
        signUp_Login.setNode(aniPane);
        signUp_Login.setByY(-400);
        signUp_Login.setDuration(Duration.millis(400));
    }

    @FXML
    protected void onJoinButtonClick(ActionEvent event) throws IOException {
        IP = ServerIPField.getText().trim();
        PORT = Integer.parseInt(ServerPortField.getText());
        Username = UsernameField.getText().trim();
        usrHash = getHash(Username);
        passHash = getHash(password.getText().trim());

        Socket s = new Socket(IP, PORT);
        DataInputStream in  = new DataInputStream(s.getInputStream());
        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.writeInt(112);
        out.flush();
        out.writeInt(usrHash);
        out.flush();
        out.writeInt(passHash);
        out.flush();

        int actionCode = in.readInt();

        if (actionCode == 0)
        {
        clienSocket = s;
        Scene ChatRoom = new Scene(FXMLLoader.load(getClass().getResource("ChatPanel.fxml")));
        Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        currentStage.setScene(ChatRoom);
        currentStage.setTitle("Tortoise Chat");
        currentStage.show();
        }
        else
        {
            errorLabel.setVisible(true);
        }

    }
    @FXML 
    protected void onSignUp()
    {
        // loginPane.setVisible(false);
        // signupPane.setVisible(true);
        signUp_Login.play();
        UsernameField.setText("");
        password.setText("");
        errorLabel.setVisible(false);
    }
    @FXML
    protected void onConfirmSignUp() throws UnknownHostException, IOException
    {
        IP = ServerIPField.getText().trim();
        PORT = Integer.parseInt(ServerPortField.getText());
        int usrHash = getHash(createUser.getText().trim());
        int pass1Hash = getHash(createPass.getText().trim());
        int pass2Hash = getHash(confirmPass.getText().trim());
        if (pass1Hash != pass2Hash)
        {
            errorLabelSignup.setVisible(true);
        }
        else{
            Socket s = new Socket(IP, PORT);
            DataInputStream in  = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            out.writeInt(111);
            out.flush();
            out.writeInt(usrHash);
            out.flush();
            out.writeInt(pass1Hash);
            out.flush();

            int actionCode = in.readInt();
            if (actionCode == 0)
            {
                // loginPane.setVisible(true);
                // signupPane.setVisible(false);
                login_SignUp.play();
                errorLabelSignup.setVisible(false);
                createUser.setText("");
                createPass.setText("");
                confirmPass.setText("");
            }
            else
            {
                errorLabelSignup.setVisible(true);
            }
        }
    }

    @FXML
    protected void onBackLogin()
    {
        // loginPane.setVisible(true);
        // signupPane.setVisible(false);
        login_SignUp.play();
        errorLabelSignup.setVisible(false);
        createUser.setText("");
        createPass.setText("");
        confirmPass.setText("");
            
    }
    public static int getHash(String input) {
        try {
            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(inputBytes);
            int hash = 0;
            for (int i = 0; i < 4; i++) {
                hash <<= 8;
                hash |= (hashBytes[i] & 0xFF);
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // This should never happen, because SHA-256 is a standard algorithm
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}