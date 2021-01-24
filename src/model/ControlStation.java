package model;

import java.io.Serializable;

public class ControlStation implements Serializable {
    private int firstCoordinateOfControlStation;
    private int secondCoordinateOfControlStation;
    public ControlStation(){

    }

    public int getFirstCoordinateOfControlStation() {
        return firstCoordinateOfControlStation;
    }

    public int getSecondCoordinateOfControlStation() {
        return secondCoordinateOfControlStation;
    }

    public void setFirstCoordinateOfControlStation(int firstCoordinateOfControlStation) {
        this.firstCoordinateOfControlStation = firstCoordinateOfControlStation;
    }

    public void setSecondCoordinateOfControlStation(int secondCoordinateOfControlSttaion) {
        this.secondCoordinateOfControlStation = secondCoordinateOfControlSttaion;
    }
}
