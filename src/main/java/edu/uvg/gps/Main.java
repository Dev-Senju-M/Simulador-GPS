package edu.uvg.gps;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/uvg/gps/main.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1200, 750);
        stage.setTitle("GPS Navigator — Plaza Tigo → UMG Jocotenango");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}