module com.tortoiseshell.tortoisechat {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.tortoiseshell.tortoisechat to javafx.fxml;
    exports com.tortoiseshell.tortoisechat;
}
