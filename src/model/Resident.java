package model;


import controller.DataAboutCoronaCity;
import controller.PageController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.*;

//import static model.City.isFieldOfMatrixFree;

public class Resident implements Runnable {
    private Long id;
    private String name;
    private String surname;
    private Integer yearOfBirth;
    private Gender gender;
    private Long houseID;
    private static Double bodyTemperature;
    Random temperature = new Random();
    private final double minTemperature = 34;
    private final double maxTemperature = 40;
    private static final String[] arrayOfNames = {"S", "M", "K", "V", "T", "U", "I", "L", "D", "B"};
    private static final String[] arrayOfSurnames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final Timer t = new Timer();
    private PositionOfResident positionOfResident;

    // From controller
    private PositionOfResident newCoordinates;
    private City city;
    private ControlStation previousControlStation;
    private PageController.SimulationStopped simulationStopped;
    private PageController.ControlStationOnPreviousRectangle controlStationOnPreviousRectangle;
    private ImageView stopSimulationImageView;
    private DataAboutCoronaCity dataAboutCoronaCity;

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

    public Resident() {
    }


    public static String getNameRandomly() {
        return arrayOfNames[new Random().nextInt(arrayOfNames.length - 1)];
    }

    public static String getSurnameRandomly() {
        return arrayOfSurnames[new Random().nextInt(arrayOfSurnames.length - 1)];
    }

    public static Gender choseGenderRandomly() {
        int gender = new Random().nextInt(2);
        if (gender == 0) {
            return Gender.Female;
        } else
            return Gender.Male;
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

    public static Double getBodyTemperature() {
        return bodyTemperature;
    }

    public static void setBodyTemperature(Double bodyTemperature) {
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

    @Override
    public void run() {
        while (!simulationStopped.isSimulationStopped) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            Direction direction = chooseDirectionOfMovement();
            //if (r instanceof Child) {
            int firstCoordinate = positionOfResident.getFirstCoordinate();
            int secondCoordinate = positionOfResident.getSecondCoordinate();
            Object field = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
            Rectangle rectangle = (Rectangle) field;
            Object fieldContent = rectangle.getUserData();
            Object nextFieldContent;
            Rectangle nextRectangle;
            Object nextField;


            Rectangle oldRectangle;

            List<ControlStation> controlStations = CityDataStore.getInstance().getControlStations();

            if (this instanceof Child) {
                Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == this.getHouseID()).findFirst();
                if (!checkBoundsForChild(direction, firstCoordinate, secondCoordinate, city)) {
                    System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
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
                if (nextFieldContent instanceof Clinic ||
                        //nextFieldContent instanceof ControlStation ||
                        nextFieldContent instanceof House ||
                        nextFieldContent instanceof Resident) {
                    System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
                } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik


                //synchronized (mapLocker) {
                if (!(fieldContent instanceof House)) {
                    oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());
                    if (previousControlStation != null) {
                        for (int i = 0; i < CityDataStore.getInstance().getControlStations().size() - 1 && controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle; i++) {
                            if (previousControlStation.getFirstCoordinateOfControlStation() == controlStations.get(i).getFirstCoordinateOfControlStation()
                                    && previousControlStation.getSecondCoordinateOfControlStation() == controlStations.get(i).getSecondCoordinateOfControlStation()) {
                                oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                oldRectangle.setUserData(previousControlStation);
                                //wasControlStationOnPreviousRectangle=false;

                            }
                        }

                    }
                    if (!controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle) {
                        oldRectangle.setFill(Color.rgb(238, 229, 222));
                        oldRectangle.setUserData(null);
                        if (nextFieldContent instanceof ControlStation) {
                            controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = true;
                            previousControlStation = (ControlStation) nextFieldContent;
                        }

                    } else {
                        previousControlStation = null;
                        controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = false;
                    }

                }


                System.out.println("Stare pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                        positionOfResident.getSecondCoordinate());
                //pozicija stanovnika u toku kretanja
                newCoordinates = new PositionOfResident(firstCoordinate, secondCoordinate);
                positionOfResident = newCoordinates;
                Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                if (newRectangle.getUserData() instanceof ControlStation) {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/thermometer+child.png")));
                } else {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/child.png")));
                }
                newRectangle.setUserData(this);

                //}
                System.out.println("Nove pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                        positionOfResident.getSecondCoordinate());
                System.out.println(name + id + ',' + direction +
                        "(" + positionOfResident.getFirstCoordinate() + "," + positionOfResident.getSecondCoordinate() + ")");
                if(checkDistanceOfResidentAndControlStation(this)){
                    if(this.bodyTemperature>37.0){
                        System.out.println("Temperatura veca od 37 stepeni!");
                    }
                }

            } else if (this instanceof Elder) {
                Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == houseID).findFirst();
                if (!checkBoundsForElder(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                    System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
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
                if (nextFieldContent instanceof Clinic ||
                        //nextFieldContent instanceof ControlStation ||
                        nextFieldContent instanceof House ||
                        nextFieldContent instanceof Resident) {
                    System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
                } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik

                //synchronized (mapLocker) {
                if (!(fieldContent instanceof House)) {
                    oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());
                    if (previousControlStation != null) {
                        for (int i = 0; i < CityDataStore.getInstance().getControlStations().size() - 1 && controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle; i++) {
                            if (previousControlStation.getFirstCoordinateOfControlStation() == controlStations.get(i).getFirstCoordinateOfControlStation()
                                    && previousControlStation.getSecondCoordinateOfControlStation() == controlStations.get(i).getSecondCoordinateOfControlStation()) {
                                oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                oldRectangle.setUserData(previousControlStation);
                                //wasControlStationOnPreviousRectangle=false;

                            }
                        }

                    }
                    if (!controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle) {
                        oldRectangle.setFill(Color.rgb(238, 229, 222));
                        oldRectangle.setUserData(null);
                        if (nextFieldContent instanceof ControlStation) {
                            controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = true;
                            previousControlStation = (ControlStation) nextFieldContent;
                        }

                    } else {
                        previousControlStation = null;
                        controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = false;
                    }

                }
                if (!checkDistance(firstCoordinate, secondCoordinate, city, direction)) {
                    System.out.println("Distanca bi bila narusena");
                    continue;
                }

                System.out.println("Stare pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                       positionOfResident.getSecondCoordinate());
                //pozicija stanovnika u toku kretanja
                newCoordinates= new PositionOfResident(firstCoordinate, secondCoordinate);
                positionOfResident = newCoordinates;
                Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                if (newRectangle.getUserData() instanceof ControlStation) {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/thermometer+elder.png")));
                } else {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/elder.png")));
                }
                //}
                System.out.println("Nove pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                        positionOfResident.getSecondCoordinate());
                System.out.println(name + id + ',' + direction +
                        "(" + positionOfResident.getFirstCoordinate() + "," + positionOfResident.getSecondCoordinate() + ")"+bodyTemperature);
                if(checkDistanceOfResidentAndControlStation(this)){
                    if(this.bodyTemperature>37.0){
                        System.out.println("Temperatura veca od 37 stepeni!");
                    }
                }

            }else if (this instanceof Adult) {
                Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == this.getHouseID()).findFirst();
                if (!checkBoundsForAdult(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                    System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
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
                if (nextFieldContent instanceof Clinic ||
                        //nextFieldContent instanceof ControlStation ||
                        nextFieldContent instanceof House ||
                        nextFieldContent instanceof Resident) {
                    System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                    continue;
                } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik

                //synchronized (mapLocker) {
                if (!(fieldContent instanceof House)) {
                    oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());
                    if (previousControlStation != null) {
                        for (int i = 0; i < CityDataStore.getInstance().getControlStations().size() - 1 && controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle; i++) {
                            if (previousControlStation.getFirstCoordinateOfControlStation() == controlStations.get(i).getFirstCoordinateOfControlStation()
                                    && previousControlStation.getSecondCoordinateOfControlStation() == controlStations.get(i).getSecondCoordinateOfControlStation()) {
                                oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                oldRectangle.setUserData(previousControlStation);
                                //wasControlStationOnPreviousRectangle=false;

                            }
                        }

                    }
                    if (!controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle) {
                        oldRectangle.setFill(Color.rgb(238, 229, 222));
                        oldRectangle.setUserData(null);
                        if (nextFieldContent instanceof ControlStation) {
                            controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = true;
                            previousControlStation = (ControlStation) nextFieldContent;
                        }

                    } else {
                        previousControlStation = null;
                        controlStationOnPreviousRectangle.wasControlStationOnPreviousRectangle = false;
                    }

                }
                if (!checkDistance(firstCoordinate, secondCoordinate, city, direction)) {
                    System.out.println("Distanca bi bila narusena");
                    continue;
                }

                System.out.println("Stare pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                        positionOfResident.getSecondCoordinate());
                //pozicija stanovnika u toku kretanja
                newCoordinates= new PositionOfResident(firstCoordinate, secondCoordinate);
                positionOfResident = newCoordinates;
                Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                if (newRectangle.getUserData() instanceof ControlStation) {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/thermometer+adult.png")));
                } else {
                    newRectangle.setFill(new ImagePattern(new Image("view/images/adult.png")));
                }
                //}
                System.out.println("Nove pozicije:" + positionOfResident.getFirstCoordinate() + "," +
                        positionOfResident.getSecondCoordinate());
                System.out.println(name + id + ',' + direction +
                        "(" + positionOfResident.getFirstCoordinate() + "," + positionOfResident.getSecondCoordinate() + ")"+bodyTemperature);
                if(checkDistanceOfResidentAndControlStation(this)){
                    if(this.bodyTemperature>37.0){
                        System.out.println("Temperatura veca od 37 stepeni!");
                    }
                }

            }



            if (stopSimulationImageView.isPressed()) {
                synchronized (simulationStopped) {
                    simulationStopped.isSimulationStopped = true;
                }

            }

        }

    }


    public void setControlStationOnPreviousRectangle(PageController.ControlStationOnPreviousRectangle controlStationOnPreviousRectangle) {
        this.controlStationOnPreviousRectangle = controlStationOnPreviousRectangle;
    }

    public void setDataAboutCoronaCity(DataAboutCoronaCity dataAboutCoronaCity) {
        this.dataAboutCoronaCity = dataAboutCoronaCity;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public void setSimulationStopped(PageController.SimulationStopped simulationStopped) {
        this.simulationStopped = simulationStopped;
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

    private boolean checkBoundsForChild(Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        switch (direction) {
            case Up -> {
                return secondCoordinate > 0;
            }
            case Left -> {
                return firstCoordinate > 0;
            }
            case Right -> {
                return firstCoordinate < city.getMatrix().length - 1;
            }
            case Bottom -> {
                return secondCoordinate < city.getMatrix().length - 1;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean checkBoundsForElder(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        System.out.println("House coordinates:" + house.getFirstCoordinateOfHouse() + "," + house.getSecondCoordinateOfHouse());
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate > house.getSecondCoordinateOfHouse() - 3);
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate > house.getFirstCoordinateOfHouse() - 3);
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate < house.getFirstCoordinateOfHouse() + 3);
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate < house.getSecondCoordinateOfHouse() + 3);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean checkBoundsForAdult(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        System.out.println("House coordinates:" + house.getFirstCoordinateOfHouse() + "," + house.getSecondCoordinateOfHouse());
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate > house.getSecondCoordinateOfHouse() - Math.round(0.25 * city.getMatrix().length));
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate > house.getFirstCoordinateOfHouse() - Math.round(0.25 * city.getMatrix().length));
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate < house.getFirstCoordinateOfHouse() + Math.round(0.25 * city.getMatrix().length));
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate < house.getSecondCoordinateOfHouse() + Math.round(0.25 * city.getMatrix().length));
            }
            default -> {
                return false;
            }
        }
    }

    private boolean checkDistance(int firstCoordinate, int secondCoordinate, City city, Direction direction) {
        //ako na sve 4 strane od izabranog polja na prvoj i drugoj poziciji nema objekta ili je on dijete,dopustice postavljanje objekta
        // if (direction == Direction.Up) {
        if ((secondCoordinate - 1) < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Elder)
                return false;
        }
        if ((secondCoordinate - 2) < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Elder)
                return false;
        }
        // } else if (direction == Direction.Bottom) {
        if ((secondCoordinate + 1) > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 1) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 1) instanceof Elder)
                return false;
        }
        if ((secondCoordinate + 2) > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 2) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 2) instanceof Elder)
                return false;
        }
        // } else if (direction == Direction.Left) {
        if ((firstCoordinate - 1) < 0)
            return true;
        else {

            if (getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Elder)
                return false;
        }
        if ((firstCoordinate - 2) < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Elder)
                return false;
        }
        // } else if (direction == Direction.Right) {
        if ((firstCoordinate + 1) > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Elder)
                return false;
        }
        if ((firstCoordinate + 2) > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate) instanceof Adult
                    || getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate) instanceof Elder)
                return false;
        }
        if (firstCoordinate - 1 < 0 || secondCoordinate - 1 < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate - 1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate - 1) instanceof Elder)
                return false;
        }
        if (firstCoordinate - 2 < 0 || secondCoordinate - 2 < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate - 2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate - 2) instanceof Elder)
                return false;
        }
        if (firstCoordinate - 1 < 0 || secondCoordinate + 1 > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate + 1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate + 1) instanceof Elder)
                return false;
        }
        if (firstCoordinate - 2 < 0 || secondCoordinate + 2 > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate + 2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate + 2) instanceof Elder)
                return false;
        }
        if (firstCoordinate + 1 > city.getMatrix().length - 1 || secondCoordinate - 1 < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate - 1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate - 1) instanceof Elder)
                return false;
        }
        if (firstCoordinate + 2 > city.getMatrix().length - 1 || secondCoordinate - 2 < 0)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate - 2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate - 2) instanceof Elder)
                return false;
        }
        if (firstCoordinate + 1 > city.getMatrix().length - 1 || secondCoordinate + 1 > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate + 1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate + 1) instanceof Elder)
                return false;
        }
        if (firstCoordinate + 2 > city.getMatrix().length - 1 || secondCoordinate + 2 > city.getMatrix().length - 1)
            return true;
        else {
            if (getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate + 2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate + 2) instanceof Elder)
                return false;
        }

      /* } else if (getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 1) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 1) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 2) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 2) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate + 2, secondCoordinate) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Elder
                || getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Adult
                || getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Elder

        )
        {return false;}*/
        return true;
    }

    private boolean checkDistanceOfResidentAndControlStation(Resident r){
        for(ControlStation controlStation:CityDataStore.getInstance().getControlStations()){
            if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()){
                return  true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()+1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()) {
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()-1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()) {
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()-1) {
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()+1) {
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()-1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()-1){
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()+1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()+1){
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()+1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()-1){
                return true;
            }
            else if(controlStation.getFirstCoordinateOfControlStation()==r.positionOfResident.getFirstCoordinate()-1 &&
                    controlStation.getSecondCoordinateOfControlStation()==r.positionOfResident.getSecondCoordinate()+1){
                return true;
            }

        }
        return false;
    }

    private Object getObjectNextToChoosedField(int firstCoordinate, int secondCoordinate) {
        return ((Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate)).getUserData();
    }

    public void setStopSimulationImageView(ImageView stopSimulationImageView) {
        this.stopSimulationImageView = stopSimulationImageView;
    }
}

//}
