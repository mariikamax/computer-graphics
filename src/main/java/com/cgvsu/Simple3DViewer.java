package com.cgvsu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Simple3DViewer extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        AnchorPane root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("3D Viewer with Transformations");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}