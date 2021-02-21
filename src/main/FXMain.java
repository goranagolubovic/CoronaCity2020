package main;

import controller.MainPageController;
import controller.PageController;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FXMain extends Application {
    @FXML
    private ImageView coronaVirusRotateImageView;

    public static void main(String[] args) throws NotChildException, NotElderException, NotAdultException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_page.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            Logger.getLogger(FXMain.class.getName()).addHandler(MainPageController.handler);
            Logger.getLogger(FXMain.class.getName()).log(Level.WARNING, e.fillInStackTrace().toString());
        }
        Scene scene1 = new Scene(root);
        primaryStage.setScene(scene1);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Corona City");
        primaryStage.show();
    }

}