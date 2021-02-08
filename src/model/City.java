package model;

import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class City implements Serializable {
    private Object[][] matrix;

    public int getCitySize() {
        return citySize;
    }

    public void setCitySize(int citySize) {
        this.citySize = citySize;
    }

    private int citySize;
    public City() {
        Random random = new Random();
        int randNumber = 15+random.nextInt(15);
       // int randNumber = 5;
        CityDataStore.getInstance().setCitySize(randNumber);
        matrix = new Object[randNumber][randNumber];
        CityDataStore.getInstance().setCitySize(randNumber);
    }
    public City(int citySize){
        matrix=new Object[citySize][citySize];
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

    public synchronized boolean checkDistanceOfField(int firstCoordinate, int secondCoordinate, Resident resident, int criterium, Class... classTypes) {
        int counter = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (firstCoordinate + i >= 0 && firstCoordinate + i < getMatrix().length &&
                        secondCoordinate + j >= 0 && secondCoordinate + j < getMatrix().length) {
                    Object obj = getFieldOfMatrix(firstCoordinate + i,secondCoordinate + j);
                    if (obj != null) {
                        Rectangle rectangle = (Rectangle) getFieldOfMatrix(firstCoordinate + i,secondCoordinate + j);
                        Object content = rectangle.getUserData();
                        if (content != null) {
                            if (resident != null) {
                                if (content instanceof Resident && !(content instanceof Child)) {
                                    Resident r = (Resident) content;
                                    if (!areResidentsHouseInmate(r, resident)) {
                                        if (Arrays.stream(classTypes).anyMatch(cl -> cl.isInstance(content))) {
                                            counter++;
                                        }
                                    }
                                }
                            }else{
                                if (Arrays.stream(classTypes).anyMatch(cl -> cl.isInstance(content))) {
                                    counter++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return counter <= criterium;
    }

    private boolean areResidentsHouseInmate(Resident r, Resident resident) {
        return (resident.getHouseID() == r.getHouseID());
    }

    public void setMatrix(int size) {
        matrix=new Object[size][size];
    }
}
