package model;

import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class City implements Serializable {
    public ArrayList<Pair> values = new ArrayList<Pair>();
    private Object[][] matrix;
    private boolean fieldOfMatrixFree = true;

    public City() {
        Random random = new Random();
        //int randNumber = 15+random.nextInt(15);
        int randNumber = 15;
        matrix = new Object[randNumber][randNumber];
    }

    public Object[][] getMatrix() {
        return matrix;
    }

    public Object getFieldOfMatrix(int i, int j) {
        return matrix[i][j];
    }

    public void setFieldOfMatrix(Object o, int i, int j) {
        matrix[i][j] = o;
    }

    public boolean checkDistanceOfField(int firstCoordinate, int secondCoordinate, int criterium, Class... classTypes) {
        int counter = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (firstCoordinate + i >= 0 && firstCoordinate + i < matrix.length &&
                        secondCoordinate + j >= 0 && secondCoordinate + j < matrix.length) {
                    Object obj = matrix[firstCoordinate + i][secondCoordinate + j];
                    if(obj!=null) {
                        Rectangle rectangle = (Rectangle) matrix[firstCoordinate + i][secondCoordinate + j];
                        Object content = rectangle.getUserData();
                        if (content != null) {
                            if (Arrays.stream(classTypes).anyMatch(cl -> cl.isInstance(content))) {
                                counter++;
                            }
                        }
                    }
                }
            }
        }
        return counter <= criterium;
    }
}
