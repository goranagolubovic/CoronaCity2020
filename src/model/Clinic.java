package model;

import java.io.Serializable;

public class Clinic implements Serializable {
    private int capacityOfClinic;
    private int firstCoordinate;
    private int secondCoordinate;

    public Clinic(int capacityOfClinic, int firstCoordinate, int secondCoordinate) {
        this.capacityOfClinic = capacityOfClinic;
        this.firstCoordinate = firstCoordinate;
        this.secondCoordinate = secondCoordinate;
    }

    public int getCapacityOfClinic() {
        return capacityOfClinic;
    }

    public void setCapacityOfClinic(int capacityOfClinic) {
        this.capacityOfClinic = capacityOfClinic;
    }

    public int getFirstCoordinate() {
        return firstCoordinate;
    }

    public void setFirstCoordinate(int firstCoordinate) {
        this.firstCoordinate = firstCoordinate;
    }

    public int getSecondCoordinate() {
        return secondCoordinate;
    }

    public void setSecondCoordinate(int secondCoordinate) {
        this.secondCoordinate = secondCoordinate;
    }
}
