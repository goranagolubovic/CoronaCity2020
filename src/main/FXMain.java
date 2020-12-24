package main;

import controller.MainPageController;
import controller.PageController;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class FXMain extends Application {
    public static void main(String[] args) throws NotChildException, NotElderException, NotAdultException {
        System.out.println("Unesite broj stanovnika:");
        Scanner scanner = new Scanner(System.in);
        int numberOfResidents = scanner.nextInt();
        launch(args);
       // Resident.setResident(numberOfResidents, City.getMatrix().length);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = null;
        //MainPageController mainPageController = new MainPageController();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_page.fxml"));
        //loader.setController(mainPageController);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.initStyle(StageStyle.UNDECORATED);
        Scene scene1 = new Scene(root);
        primaryStage.setScene(scene1);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Corona City");
        primaryStage.show();
        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/page.fxml"));
        PageController controller = new PageController();
        loader.setController(controller);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root, 1240, 700);
        scene.getStylesheets().add("view/style.css");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Corona City");
        primaryStage.show();

    }*/
    }

}