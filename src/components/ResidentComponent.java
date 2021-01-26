package components;


import controller.DataAboutCoronaCity;
import controller.PageController;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import model.*;
import util.JavaFXUtil;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

//import static model.City.isFieldOfMatrixFree;

public abstract class ResidentComponent implements Runnable {
    protected Resident resident;

   /* public static class BackToHome{
        public static boolean goBackToHome;
        public BackToHome(boolean o){
            goBackToHome= o;
        }
    }*/
    // From controller
    protected PositionOfResident newCoordinates;
    protected City city;
    protected ControlStation previousControlStation;
    protected House previousHouse;
    protected Clinic previousClinic;
    protected PageController.SimulationStopped simulationStopped;
    protected DataAboutCoronaCity dataAboutCoronaCity;
    public static final Object locker = new Object();
    private static final Object lockerBackToHome=new Object();
    public static Stack<Alarm> stackOfAlarms = new Stack<Alarm>();

    public ResidentComponent(Resident resident) {
        this.resident = resident;
    }

    @Override
    public void run() {
        boolean wait = true;
        boolean backToHome = false;
        List<Clinic> clinics = CityDataStore.getInstance().getClinics();
        while (!simulationStopped.isSimulationStopped) {
            if (wait) {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            List<Resident>recoveredResidents;
            wait = movement(backToHome);
            if (checkDistanceOfResidentAndControlStation()) { //&& PageController.isThreadRunning
                if (resident.getBodyTemperature() > 37.0) {
                    stackOfAlarms.push(new Alarm(resident.getCurrentPositionOfResident().getFirstCoordinate(), resident.getCurrentPositionOfResident().getSecondCoordinate(), resident.getHouseID()));
                    synchronized (PageController.lockerInfectedPerson) {
                        try {
                            PageController.lockerInfectedPerson.wait();
                            Rectangle rectangle = (Rectangle) city.getFieldOfMatrix( resident.getCurrentPositionOfResident().getFirstCoordinate(),  resident.getCurrentPositionOfResident().getSecondCoordinate());
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
                            for (int i = 0, clinicsSize = clinics.size(); i < clinicsSize; i++) {
                                Clinic clinic = clinics.get(i);
                                if (clinic.addInfectedResident(resident)) {
                                    System.out.println("stanovnik "+resident.getId()+" je dodan u kliniku "+clinic.getCapacityOfClinic());
                                    break;
                                }else {
                                    if(i==clinicsSize-1){
                                        System.out.println("Kapaciteti klinika su popunjeni.Kreirajte novu kliniku.");
                                    }
                                }
                            }
                            /*backToHome = ComponentsCityDataStore.getInstance()
                                    .getResidents()
                                    .stream()
                                    .anyMatch(res -> res.resident.getHouseID() == resident.getHouseID() &&
                                            res.checkDistanceOfResidentAndControlStation() &&
                                            res.resident.getBodyTemperature() > 37);*/
                            return;
                            //PageController.listOfActiveThreads.remove(PageController.residentThread);
                            //}
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            backToHome = ComponentsCityDataStore.getInstance()
                    .getResidents()
                    .stream()
                    .anyMatch(res -> res.resident.getId() != resident.getId() &&
                            res.resident.getHouseID() == resident.getHouseID() &&
                            res.checkDistanceOfResidentAndControlStation() &&
                            res.resident.getBodyTemperature() > 37);

        }

    }

    public boolean movement(boolean goBackToHome) {
        Direction direction = chooseDirectionOfMovement();
        int firstCoordinate = resident.getCurrentPositionOfResident().getFirstCoordinate();
        int secondCoordinate = resident.getCurrentPositionOfResident().getSecondCoordinate();
        Direction directionToHome;
        if ( goBackToHome && resident.getCurrentPositionOfResident().getFirstCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getFirstCoordinateOfHouse()
                && resident.getCurrentPositionOfResident().getSecondCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getSecondCoordinateOfHouse()) {
            return false;
        }
        else
        {
            directionToHome=findOptimalDirectionToGetHome(resident.getCurrentPositionOfResident(),resident.getHouseWithConcretID(resident.getHouseID()));
        }
        Object nextFieldContent;
        Rectangle nextRectangle;
        Object nextField;
        Object field = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
        Rectangle rectangle = (Rectangle) field;
        Object fieldContent = rectangle.getUserData();
        Rectangle oldRectangle;

        if (!goBackToHome) {
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
        }
        else {
            System.out.println("Povratak kuci.."+resident.getId());
            switch (directionToHome) {
                case Up -> secondCoordinate--;
                case Left -> firstCoordinate--;
                case Right -> firstCoordinate++;
                case Bottom -> secondCoordinate++;
            }
        }
            nextField = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
            nextRectangle = (Rectangle) nextField;
            nextFieldContent = nextRectangle.getUserData();
            synchronized (locker) {
                if (!checkDistance(firstCoordinate, secondCoordinate)) {
                    return false;
                }
            }
            if (nextFieldContent instanceof Resident) {
                return false;
            }

            if (/*!(fieldContent instanceof House) &&*/ newCoordinates!=null) {
                synchronized (locker) {
                    oldRectangle = (Rectangle) city.getFieldOfMatrix(newCoordinates.getFirstCoordinate(), newCoordinates.getSecondCoordinate());

                    if (previousControlStation != null) {
                        JavaFXUtil.runAndWait(() -> {
                            oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                        });
                        oldRectangle.setUserData(previousControlStation);
                    }
                    else if(previousHouse!=null){
                        JavaFXUtil.runAndWait(()->{
                            oldRectangle.setFill(new ImagePattern(new Image("view/images/home.png")));
                        });
                        oldRectangle.setUserData(previousHouse);
                    }
                    else if(previousClinic!=null){
                        JavaFXUtil.runAndWait(()->{
                            oldRectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                        });
                        oldRectangle.setUserData(previousClinic);
                    }
                    else
                     {
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
            }
            else if(nextFieldContent instanceof Clinic){
                previousClinic=(Clinic) nextFieldContent;
            }
            else if(nextFieldContent instanceof House){
                previousHouse=(House) nextFieldContent;
            }
            else {
                previousControlStation = null;
                previousClinic=null;
                previousHouse=null;

            }
            //pozicija stanovnika u toku kretanja
            newCoordinates = new PositionOfResident(firstCoordinate, secondCoordinate);
            resident.setCurrentPositionOfResident(newCoordinates.getFirstCoordinate(),newCoordinates.getSecondCoordinate());
        if (goBackToHome && resident.getCurrentPositionOfResident().getFirstCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getFirstCoordinateOfHouse()
                && resident.getCurrentPositionOfResident().getSecondCoordinate() == resident.getHouseWithConcretID(resident.getHouseID()).getSecondCoordinateOfHouse()) {
                 synchronized (Clinic.lockerInfectedInmate){
                     try {
                         Clinic.lockerInfectedInmate.wait();
                         return true;
                         //simulationStopped.isSimulationStopped=false;
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
                 return false;
        }
            Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
            synchronized (locker) {
                if (newRectangle.getUserData() instanceof ControlStation) {
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithThermometer())));
                }
                else if(newRectangle.getUserData() instanceof Clinic){
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithClinic())));
                }
                else if(newRectangle.getUserData() instanceof House && resident.isResidentInHouse()){
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(new Image("view/images/home.png"))));
                }
                else if(newRectangle.getUserData() instanceof House){
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResidentWithHouse())));
                }
                else {
                    JavaFXUtil.runAndWait(() -> newRectangle.setFill(new ImagePattern(getImageOfResident())));
                    newRectangle.setUserData(this);
                }
            }
        return true;
    }


    public Direction findOptimalDirectionToGetHome(PositionOfResident currentPositionOfResident, House houseWithConcretID) {
        //ako je stanovnik gore desno od kuce ili gore iznad kuce ili gore lijevo od kuce
        if(currentPositionOfResident.getSecondCoordinate()<houseWithConcretID.getSecondCoordinateOfHouse()){
            return Direction.Bottom;
        }
        //ako je stanovnik  dolje desno od kuce,ili dolje ispod kuce,ili dolje lijevo od kuce
        else if(currentPositionOfResident.getSecondCoordinate()>houseWithConcretID.getSecondCoordinateOfHouse()){
            return Direction.Up;
        }
        //ako je stanovnik lijevo od kuce
        else if(currentPositionOfResident.getSecondCoordinate()==houseWithConcretID.getSecondCoordinateOfHouse()
        && currentPositionOfResident.getFirstCoordinate()<houseWithConcretID.getFirstCoordinateOfHouse()){
            return Direction.Right;
        }
        //ako je stanovnik desno od kuce
        else if(currentPositionOfResident.getSecondCoordinate()==houseWithConcretID.getSecondCoordinateOfHouse()
                && currentPositionOfResident.getFirstCoordinate()>houseWithConcretID.getFirstCoordinateOfHouse()){
            return Direction.Left;
        }
        else
            return null;

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

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }
}

//}
