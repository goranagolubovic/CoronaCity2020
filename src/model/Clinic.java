package model;

import java.io.Serializable;

public class Clinic implements Serializable {
    private int capacityOfClinic;
    public Clinic(int numberOfResidents){
     capacityOfClinic=numberOfResidents;
    }
}
