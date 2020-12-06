package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import model.City;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class PageController implements Initializable {
    City city;

    public PageController() {
        city = new City();
    }

    @FXML
    private GridPane MatrixWrapper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
    }

    public void initMap() {
        GridPane map = new GridPane();

        double gridWidth = 400;
        double gridHeight = 400;
        double cellHeight = 400.0 / city.getMatrix().length;
        double cellWidth = 400.0 / city.getMatrix().length;

        for (int i = 0; i < city.getMatrix().length; i++) {
            map.getColumnConstraints().add(new ColumnConstraints(cellWidth));
            map.getRowConstraints().add(new RowConstraints(cellHeight));
        }
        System.out.println(map.getPrefHeight() + ";" + map.getPrefWidth());
        MatrixWrapper.add(map, 1, 1, 4, 4);
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                rectangle.getStyleClass().add("rectangle-map");
                map.add(rectangle, i, j);
            }
        }
    }
}
