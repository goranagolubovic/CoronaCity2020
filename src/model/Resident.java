package model;


import controller.DataAboutCoronaCity;
import controller.PageController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import util.JavaFXUtil;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

//import static model.City.isFieldOfMatrixFree;

public abstract class Resident implements Runnable, Serializable {
    protected Long id;
    protected String name;
    protected String surname;
    protected Integer yearOfBirth;
    protected Gender gender;
    protected Long houseID;
    protected static Double bodyTemperature;
    Random temperature = new Random();
    protected final double minTemperature = 34;
    protected final double maxTemperature = 40;
    protected static final String[] arrayOfNames = {"S", "M", "K", "V", "T", "U", "I", "L", "D", "B"};
    protected static final String[] arrayOfSurnames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    protected final Timer t = new Timer();
    protected PositionOfResident positionOfResident;

    // From controller
    protected PositionOfResident newCoordinates;
    protected City city;
    protected ControlStation previousControlStation;
    protected PageController.SimulationStopped simulationStopped;
    protected DataAboutCoronaCity dataAboutCoronaCity;
    public static final Object locker = new Object();
    public static Stack<Alarm> stackOfAlarms = new Stack<Alarm>();

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
                DecimalFormat df = new DecimalFormat("0.0");
                bodyTemperature = Double.parseDouble(df.format(minTemperature + (temperature.nextDouble() * (maxTemperature - minTemperature))));
            }
        }, 0, 10_000);
        positionOfResident = new PositionOfResident(0, 0);
    }

    @Override
    public void run() {
        boolean wait = true;
        boolean backToHome = false;
        while (!simulationStopped.isSimulationStopped) {
            if (wait) {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            wait = movement(backToHome);
            if (checkDistanceOfResidentAndControlStation()) { //&& PageController.isThreadRunning
                if (bodyTemperature > 50.0) {
                    stackOfAlarms.push(new Alarm(getCurrentPositionOfResident().getFirstCoordinate(), getCurrentPositionOfResident().getSecondCoordinate(), getHouseID()));
                    synchronized (PageController.lockerInfectedPerson) {
                        try {

                            PageController.lockerInfectedPerson.wait();

                            Rectangle rectangle = (Rectangle) city.getFieldOfMatrix(getCurrentPositionOfResident().getFirstCoordinate(), getCurrentPositionOfResident().getSecondCoordinate());
                            if (rectangle.getUserData() instanceof Resident) {
                                rectangle.setUserData(null);
                                JavaFXUtil.runAndWait(() -> {

                                    rectangle.setFill(Color.rgb(238, 229, 222));
                                });
                            } else {
                                JavaFXUtil.runAndWait(() -> {
                                    rectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                });
                                rectangle.setUserData(previousControlStation);
                            }
                            return;
                            //PageController.listOfActiveThreads.remove(PageController.residentThread);
                            //}
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            backToHome = CityDataStore.getInstance()
                    .getResidents()
                    .stream()
                    .anyMatch(res -> res.getHouseID() == houseID &&
                            res.checkDistanceOfResidentAndControlStation() &&
                            res.getBodyTemperature() > 50);

        }

    }

    public boolean movement(boolean backToHome) {
        Direction direction = chooseDirectionOfMovement();
        int firstCoordinate = positionOfResident.getFirstCoordinate();
        int secondCoordinate = positionOfResident.getSecondCoordinate();

        Object nextFieldContent;
        Rectangle nextRectangle;
        Object nextField;
        Object field = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
        Rectangle rectangle = (Rectangle) field;
        Object fieldContent = rectangle.getUserData();
        Rectangle oldRectangle;

        if (!backToHome) {
            Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == houseID).findFirst();
            if (optionalHouse.isEmpty() || !checkBounds(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                return false;
            }

            switch (direction) {
                case Up -> secondCoordinate--;
                case Left -> firstCoordinate--;
                case Right -> firstCoordinate++;
                case Bottom -> secondCoordinate++;
            }
            nextField = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
            nextRectangle = (Rectangle) nextField;
            nextFieldContent = nextRectangle.getUserData();
            synchronized (locker) {
                if (!checkDistance(firstCoordinate, secondCoordinate)) {
                    return false;
                }
            }
            if (nextFieldContent instanceof Clinic ||
                    nextFieldContent instanceof House ||
                    nextFieldContent instanceof Resident) {
                return false;
            }

            if (!(fieldContent instanceof House)) {
                synchronized (locker) {
                    oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());

                    if (previousControlStation != null) {
                        JavaFXUtil.runAndWait(() -> {
                            oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                        });
                        oldRectangle.setUserData(previousControlStation);
                    } else {
                        JavaFXUtil.runAndWait(() -> oldRectangle.setFill(Color.rgb(238, 229, 222)));
                        oldRectangle.setUserData(null);
                    }
//                synchronized (PageController.lockerThreadRunning) {
//                    if (!PageController.isThreadRunning) {
//                        return false;
//                    }
//                }

                }
            }
            if (nextFieldContent instanceof ControlStation) {
                previousControlStation = (ControlStation) nextFieldContent;
            }else {
                previousControlStation = null;
            }
            //pozicija stanovnika u toku kretanja
            newCoordinates = new PositionOfResident(firstCoordinate, secondCoordinate);
            positionOfResident = newCoordinates;
            Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
            synchronized (locker) {
                if (newRectangle.getUserData() instanceof ControlStation) {
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithThermometer())));
                } else {
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResident())));
                    newRectangle.setUserData(this);
                }
            }

        } else { //algoritam za vracanje stanovnika kuci najkracim putem
//            while (getHouseWithConcretID(houseID).getFirstCoordinateOfHouse() != getCurrentPositionOfResident().getFirstCoordinate()) {
//                while (getHouseWithConcretID(houseID).getSecondCoordinateOfHouse() != getCurrentPositionOfResident().getSecondCoordinate()) {
//
//                }
//
//            }
        }
        return true;
    }


    public Direction chooseDirectionOfMovement() {
        Random random = new Random();
        int direction = random.nextInt(4);
        return switch (direction) {
            case 0 -> Direction.Up;
            case 1 -> Direction.Bottom;
            case 2 -> Direction.Right;
            case 3 -> Direction.Left;
            default -> null;
        };
    }

    public boolean checkDistanceOfResidentAndControlStation() {
        for (ControlStation controlStation : CityDataStore.getInstance().getControlStations()) {
            if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() + 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() + 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == positionOfResident.getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == positionOfResident.getSecondCoordinate() + 1) {
                return true;
            }

        }
        return false;
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

    // Abstract methods
    public abstract boolean checkBounds(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city);

    public abstract Image getImageOfResident();

    public abstract Image getImageOfResidentWithThermometer();

    public abstract boolean checkDistance(int firstCoordinate, int secondCoordinate);

    // Setters and getters
    public void setDataAboutCoronaCity(DataAboutCoronaCity dataAboutCoronaCity) {
        this.dataAboutCoronaCity = dataAboutCoronaCity;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setSimulationStopped(PageController.SimulationStopped simulationStopped) {
        this.simulationStopped = simulationStopped;
    }


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
        Resident.bodyTemperature = bodyTemperature;
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
    public House getHouseWithConcretID(Long id){
        for(House house:CityDataStore.getInstance().getHouses()){
            if(house.getId()==id){
                return house;
            }
        }
        return null;
    }

}

//}
