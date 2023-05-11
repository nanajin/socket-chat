module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.example.chat.client to javafx.fxml;
    opens com.example.chat to javafx.fxml;
    exports com.example.chat.client;
    exports com.example.chat;
}