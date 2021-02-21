package components;


import controller.DataAboutCoronaCity;
import controller.MainPageController;
import controller.PageController;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import model.*;
import util.JavaFXUtil;

import javax.naming.Context;
import javax.xml.crypto.Data;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class ResidentComponent implements Runnable {
    protected Resident resident;

    // From controller
    public PositionOfResident newCoordinates;
    protected City city;
    public ControlStation previousControlStation;
    public House previousHouse;
    public Clinic previousClinic;
    protected PageController.SimulationStopped simulationStopped;
    protected DataAboutCoronaCity dataAboutCoronaCity;
    public static final Object locker = new Object();
    public static Stack<Alarm> stackOfAlarms = new Stack<Alarm>();
    protected boolean backToHome = false;
    protected final Object lockerInfected = new Object();
    private DataAboutMovement dataAboutMovement = new DataAboutMovement();

    public ResidentComponent(Resident resident) {
        this.resident = resident;
    }

    @Override
    public void run() {
        Logger.getLogger(ResidentComponent.class.getName()).addHandler(MainPageController.handler);
        boolean wait = true;
        List<Clinic> clinics = CityDataStore.getInstance().getClinics();
        while (!simulationStopped.isSimulationStopped) {
            if (wait) {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e1) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING, e1.fillInStackTrace().toString());
                }
            }

            wait = movement();
            //Ako je zarazen
            if (checkDistanceOfResidentAndControlStation() && isResidentGetOutOfTheHouse(resident) && resident.getBodyTemperature() > 37.0) { //&& PageController.isThreadRunning
                resident.setInfected(true);

                stackOfAlarms.push(new Alarm(resident.getCurrentPositionOfResident().getFirstCoordinate(), resident.getCurrentPositionOfResident().getSecondCoordinate(), resident.getHouseID(), resident));
                synchronized (PageController.lockerInfectedPerson) {
                    try {
                        Rectangle rectangle = (Rectangle) city.getFieldOfMatrix(resident.getCurrentPositionOfResident().getFirstCoordinate(), resident.getCurrentPositionOfResident().getSecondCoordinate());
                        PageController.lockerInfectedPerson.wait();
                        if (!(rectangle.getUserData() instanceof ControlStation || rectangle.getUserData() instanceof Clinic
                                || rectangle.getUserData() instanceof House)) {
                            rectangle.setUserData(null);
                            Platform.runLater(() -> {
                                rectangle.setFill(Color.rgb(238, 229, 222));
                            });
                        } else if (rectangle.getUserData() instanceof ControlStation) {
                            Platform.runLater(() -> {
                                rectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                            });
                            rectangle.setUserData(previousControlStation);
                        } else if (rectangle.getUserData() instanceof House) {
                            Platform.runLater(() -> {
                                rectangle.setFill(new ImagePattern(new Image("view/images/home.png")));
                            });
                            rectangle.setUserData(previousHouse);
                        } else {
                            Platform.runLater(() -> {
                                rectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                            });
                            rectangle.setUserData(previousClinic);
                        }

                    } catch (InterruptedException ex) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING, ex.fillInStackTrace().toString());
                    }

                }
                while (resident.isInfected()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                backToHome = false;
            }
            // Ako nije zaražen..
            else {
                backToHome = ComponentsCityDataStore.getInstance()
                        .getResidents()
                        .stream()
                        .anyMatch(res -> res.resident.getId() != resident.getId() &&
                                res.resident.getHouseID() == resident.getHouseID() &&
                                res.resident.isInfected() && res.resident.isInClinic());

                if (backToHome && resident.getCurrentPositionOfResident().getFirstCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getFirstCoordinateOfHouse()
                        && resident.getCurrentPositionOfResident().getSecondCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getSecondCoordinateOfHouse()) {
                    List<ResidentComponent> listOfInfectedHouseMates = findInfectedHouseMates(resident);
                    while (listOfInfectedHouseMates.stream().anyMatch(res -> res.resident.isInfected())) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    backToHome = false;
                }
            }
//

        }

    }

    private boolean isResidentGetOutOfTheHouse(Resident resident) {
        int firstCoordinate = resident.getCurrentPositionOfResident().getFirstCoordinate();
        int secondCoordinate = resident.getCurrentPositionOfResident().getSecondCoordinate();
        House house = resident.getHouseWithConcretID(resident.getHouseID());
        return !(firstCoordinate == house.getFirstCoordinateOfHouse() && secondCoordinate == house.getSecondCoordinateOfHouse());
    }

    public boolean movement() {
        Logger.getLogger(ResidentComponent.class.getName()).addHandler(MainPageController.handler);
        Direction direction = chooseDirectionOfMovement();
        int firstCoordinate = resident.getCurrentPositionOfResident().getFirstCoordinate();
        int secondCoordinate = resident.getCurrentPositionOfResident().getSecondCoordinate();
        // Promjena pravca u slučaju da ide ka kući.
        if (backToHome) {
            direction = findOptimalDirectionToGetHome(resident.getCurrentPositionOfResident(), resident.getHouseWithConcretID(resident.getHouseID()));
        }
        Object nextFieldContent;
        Rectangle nextRectangle;
        Object nextField;
        Object field = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
        Rectangle rectangle = (Rectangle) field;
        Object fieldContent = rectangle.getUserData();
        Rectangle oldRectangle;

        if (!backToHome) {
            Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == resident.getHouseID()).findFirst();
            if (optionalHouse.isEmpty() || !checkBounds(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                return false;
            }

            switch (direction) {
                case Up -> secondCoordinate--;
                case Left -> firstCoordinate--;
                case Right -> firstCoordinate++;
                case Bottom -> secondCoordinate++;
            }
        } else {
            switch (direction) {
                case Up -> secondCoordinate--;
                case Left -> firstCoordinate--;
                case Right -> firstCoordinate++;
                case Bottom -> secondCoordinate++;
            }
        }
        synchronized (locker) {
            if (!checkDistance(firstCoordinate, secondCoordinate, resident)) {
                return true;
            }
        }
        nextField = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
        nextRectangle = (Rectangle) nextField;
        nextFieldContent = nextRectangle.getUserData();
        if (nextFieldContent instanceof Resident) {
            return false;
        }

        if (newCoordinates != null) {
            synchronized (locker) {
                oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());

                if (previousControlStation != null) {
                    JavaFXUtil.runAndWait(() -> {
                        oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                    });
                    oldRectangle.setUserData(previousControlStation);
                } else if (previousHouse != null) {
                    JavaFXUtil.runAndWait(() -> {
                        oldRectangle.setFill(new ImagePattern(new Image("view/images/home.png")));
                    });
                    oldRectangle.setUserData(previousHouse);
                } else if (previousClinic != null) {
                    JavaFXUtil.runAndWait(() -> {
                        oldRectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                    });
                    oldRectangle.setUserData(previousClinic);
                } else {
                    JavaFXUtil.runAndWait(() -> oldRectangle.setFill(Color.rgb(238, 229, 222)));
                    oldRectangle.setUserData(null);
                }
                previousHouse = null;
                previousClinic = null;
                previousControlStation = null;
            }
        }
        if (nextFieldContent instanceof ControlStation) {
            previousControlStation = (ControlStation) nextFieldContent;
        } else if (nextFieldContent instanceof Clinic) {
            previousClinic = (Clinic) nextFieldContent;
        } else if (nextFieldContent instanceof House) {
            previousHouse = (House) nextFieldContent;
        } else {
            previousControlStation = null;
            previousClinic = null;
            previousHouse = null;

        }
        //pozicija stanovnika u toku kretanja
        newCoordinates = new PositionOfResident(firstCoordinate, secondCoordinate);
        resident.setCurrentPositionOfResident(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());
        if (backToHome && resident.getCurrentPositionOfResident().getFirstCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getFirstCoordinateOfHouse()
                && resident.getCurrentPositionOfResident().getSecondCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getSecondCoordinateOfHouse()) {
            List<ResidentComponent> listOfInfectedHouseMates = findInfectedHouseMates(resident);
            while (listOfInfectedHouseMates.stream().anyMatch(housemate -> housemate.resident.isInfected())) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            backToHome = false;
            return true;
        }

        Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
        synchronized (locker) {
            if (newRectangle.getUserData() instanceof ControlStation) {
                JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithThermometer())));
            } else if (newRectangle.getUserData() instanceof Clinic) {
                JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithClinic())));
            } else if (newRectangle.getUserData() instanceof House && resident.isResidentInHouse()) {
                JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(new Image("view/images/home.png"))));
            } else if (newRectangle.getUserData() instanceof House) {
                JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithHouse())));
            } else {
                JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResident())));
                newRectangle.setUserData(resident);
            }
        }
        File file = new File("dataAboutMovement.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file, true);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(resident.getName() + resident.getId() + "," + direction + ",(" + resident.getCurrentPositionOfResident().getSecondCoordinate()
                    + "," + resident.getCurrentPositionOfResident().getFirstCoordinate() + ")");
            bufferedWriter.write("\r\n");
            bufferedWriter.close();

        } catch (IOException e) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING, e.fillInStackTrace().toString());
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("dataAboutMovement.txt"));
            String content = br.readLine();
            String s = "";
            while (content != null) {
                s += content + "\r\n";
                content = br.readLine();
            }
            Text text = new Text();
            text.setText(s);
            Platform.runLater(() -> ComponentsCityDataStore.getInstance().getPageController().getScrollPane().setContent(text));
        } catch (IOException e) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING, e.fillInStackTrace().toString());
        }
        dataAboutMovement.setDirection(direction);
        dataAboutMovement.setPositionOfResident(resident.getCurrentPositionOfResident());
        dataAboutMovement.setName(resident.getName());
        dataAboutMovement.setId(resident.getId());
        return true;
    }

    private List<ResidentComponent> findInfectedHouseMates(Resident resident) {
        return ComponentsCityDataStore
                .getInstance()
                .getResidents()
                .stream()
                .filter((r) -> resident.getHouseID() == r.resident.getHouseID() &&
                        r.getResident().getBodyTemperature() > 37.0 &&
                        r.getResident().getId() != resident.getId())
                .collect(Collectors.toList());
    }


    public Direction findOptimalDirectionToGetHome(PositionOfResident currentPositionOfResident, House houseWithConcretID) {
        //ako je stanovnik gore desno od kuce ili gore iznad kuce ili gore lijevo od kuce
        if (currentPositionOfResident.getSecondCoordinate() < houseWithConcretID.getSecondCoordinateOfHouse()) {
            return Direction.Bottom;
        }
        //ako je stanovnik  dolje desno od kuce,ili dolje ispod kuce,ili dolje lijevo od kuce
        else if (currentPositionOfResident.getSecondCoordinate() > houseWithConcretID.getSecondCoordinateOfHouse()) {
            return Direction.Up;
        }
        //ako je stanovnik lijevo od kuce
        else if (currentPositionOfResident.getSecondCoordinate() == houseWithConcretID.getSecondCoordinateOfHouse()
                && currentPositionOfResident.getFirstCoordinate() < houseWithConcretID.getFirstCoordinateOfHouse()) {
            return Direction.Right;
        }
        //ako je stanovnik desno od kuce
        else if (currentPositionOfResident.getSecondCoordinate() == houseWithConcretID.getSecondCoordinateOfHouse()
                && currentPositionOfResident.getFirstCoordinate() > houseWithConcretID.getFirstCoordinateOfHouse()) {
            return Direction.Left;
        } else
            return Direction.Bottom;

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
            if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate()) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() + 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() + 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() + 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() - 1) {
                return true;
            } else if (controlStation.getFirstCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getFirstCoordinate() - 1 &&
                    controlStation.getSecondCoordinateOfControlStation() == resident.getCurrentPositionOfResident().getSecondCoordinate() + 1) {
                return true;
            }

        }
        return false;
    }

    @Override
    public String toString() {
        return resident.toString();
    }

    // Abstract methods
    public abstract boolean checkBounds(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city);

    public abstract Image getImageOfResident();

    public abstract Image getImageOfResidentWithThermometer();

    public abstract Image getImageOfResidentWithClinic();

    public abstract Image getImageOfResidentWithHouse();

    public abstract boolean checkDistance(int firstCoordinate, int secondCoordinate, Resident resident);

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

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public void setBackToHome(boolean backToHome) {
        this.backToHome = backToHome;
    }

    public Object getLockerInfected() {
        return lockerInfected;
    }
}

