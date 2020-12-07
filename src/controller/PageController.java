package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import model.City;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class PageController implements Initializable {
    City city;
    private static String clinic;
    private static String playButton;

    public Object locker = new Object();

    public PageController() {
        city = new City();
    }

    @FXML
    private GridPane MatrixWrapper;
    @FXML
    private GridPane map;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
    }

    public void initMap() {
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;

        map.getColumnConstraints().clear();
        map.getRowConstraints().clear();
        for (int i = 0; i < city.getMatrix().length; i++) {
            map.getColumnConstraints().add(new ColumnConstraints(cellWidth));
            map.getRowConstraints().add(new RowConstraints(cellHeight));
        }
        System.out.println(map.getPrefHeight() + ";" + map.getPrefWidth());
        //MatrixWrapper.add(map, 4, 2, 4, 4);
        //Image clinic=new Image(getClass().getResourceAsStream("clinic.png"));
        Image clinic = new Image("view/images/clinic.png");
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                rectangle.getStyleClass().add("rectangle-map");
                rectangle.setFill(Color.rgb(238, 229, 222));
                if ((i == 0 && j == 0) || (i == 0 && j == (city.getMatrix().length - 1)) || (j == 0 && i == (city.getMatrix().length - 1)) || (i == (city.getMatrix().length - 1) && j == (city.getMatrix().length - 1))) {
                    rectangle.setFill(new ImagePattern(clinic));
                }
                map.add(rectangle, i, j);

            }
        }
    }

    public void loadProperty() throws IOException {
        Properties properties = new Properties();
        String propertiesFileName = "view/properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
        if (inputStream != null) {
            properties.load(inputStream);
        }
        clinic = properties.getProperty("clinic");
        playButton = properties.getProperty("playButton");
    }

    @FXML
    private void allowMovement(MouseEvent e) {
        Thread thread = new Thread(() -> {
            synchronized (locker) {

                locker.notify();
            }


            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Kretanje stanovnika je počelo. 😊");
                a.show();
            });
        });
        thread.start();
    }

    @FXML
    private void sendAmbulance(MouseEvent e) {
        Thread thread = new Thread(() -> {
            try {
                synchronized (locker) {

                    locker.wait();
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Poslano je ambulanto vozilo. 😊");
                a.show();
            });
        });
        thread.start();
    }

}
