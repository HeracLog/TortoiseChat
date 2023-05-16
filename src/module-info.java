module main.java.com.tortoise.chat.tortoisechat {
    requires javafx.controls;
    requires javafx.fxml;


    opens main.java.com.tortoise.chat.tortoisechat to javafx.fxml;
    exports main.java.com.tortoise.chat.tortoisechat;
}