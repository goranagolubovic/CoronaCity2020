package model;


import components.ResidentComponent;
import controller.DataAboutCoronaCity;
import controller.PageController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import util.JavaFXUtil;

import javax.swing.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

//import static model.City.isFieldOfMatrixFree;

public abstract class Resident implements Serializable {
    protected Long id;
    protected String name;
    protected String surname;
    protected Integer yearOfBirth;
    protected Gender gender;
    protected Long houseID;
    protected Double bodyTemperature;
    protected final double minTemperature = 34;
    protected final double maxTemperature = 40;
    protected static final String[] arrayOfNames = {"S", "M", "K", "V", "T", "U", "I", "L", "D", "B"};
    protected static final String[] arrayOfSurnames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    protected Queue<Double> threeLastBodyTemperatures = new CircularFifoQueue<Double>(3);
    protected PositionOfResident positionOfResident;
    protected boolean isInfected = false;


    public Resident(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
        this.gender = gender;
        this.houseID = houseID;
        Resident thisResident = this;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Random temperature = new Random();
                DecimalFormat df = new DecimalFormat("0.0");
                bodyTemperature = Double.parseDouble(df.format(minTemperature + (temperature.nextDouble() * (maxTemperature - minTemperature))));
                threeLastBodyTemperatures.add(bodyTemperature);
            }
        }, 0, 10_000);
        positionOfResident = new PositionOfResident(0, 0);
    }

    @Override
    public String toString() {
        return "Resident{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", yearOfBirth=" + yearOfBirth +
                ", gender=" + gender +
                ", houseID=" + houseID +
                ", bodyTemperature=" + bodyTemperature +
                '}';
    }

    public abstract ResidentComponent mapToComponent();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Long getHouseID() {
        return houseID;
    }

    public void setHouseID(Long houseID) {
        this.houseID = houseID;
    }

    public Double getBodyTemperature() {
        return bodyTemperature;
    }

    public void setBodyTemperature(Double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PositionOfResident getCurrentPositionOfResident() {
        return positionOfResident;
    }

    public void setCurrentPositionOfResident(int i, int j) {
        positionOfResident.setFirstCoordinate(i);
        positionOfResident.setSecondCoordinate(j);
    }

    public static String getNameRandomly() {
        return arrayOfNames[new Random().nextInt(arrayOfNames.length - 1)];
    }

    public static String getSurnameRandomly() {
        return arrayOfSurnames[new Random().nextInt(arrayOfSurnames.length - 1)];
    }

    public House getHouseWithConcretID(Long id) {
        for (House house : CityDataStore.getInstance().getHouses()) {
            if (house.getId() == id) {
                return house;
            }
        }
        return null;
    }

    public boolean isResidentInHouse() {
        return (getCurrentPositionOfResident().getFirstCoordinate() == getHouseWithConcretID(this.getHouseID()).getFirstCoordinateOfHouse()
                && getCurrentPositionOfResident().getSecondCoordinate() == getHouseWithConcretID(this.getHouseID()).getSecondCoordinateOfHouse());
    }

    public Queue<Double> getThreeLastBodyTemperatures() {
        return threeLastBodyTemperatures;
    }

    public void setThreeLastBodyTemperatures(Queue<Double> threeLastBodyTemperatures) {
        this.threeLastBodyTemperatures = threeLastBodyTemperatures;
    }

    public boolean isInfected() {
        return isInfected;
    }

    public void setInfected(boolean infected) {
        isInfected = infected;
    }

    public boolean isInClinic() {
        List<Clinic> listOfClinics = CityDataStore.getInstance().getClinics();
        for (Clinic c : listOfClinics) {
            if (c.getInfectedResidents().stream().anyMatch(res -> res.getId() == id))
                return true;
        }
        return false;
    }
}

//}
