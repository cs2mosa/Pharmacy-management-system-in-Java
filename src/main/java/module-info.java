module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires java.net.http;
    requires com.google.gson;

    opens com.example.demo1 to javafx.fxml;
    opens Class_model to com.google.gson;
    opens Http to com.google.gson;

    exports GUI;
    exports Class_model;
    exports Service_Interfaces;
    exports Http;
}
