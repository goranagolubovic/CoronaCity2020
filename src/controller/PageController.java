package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class PageController implements Initializable {

     private class SimulationStopped{
        private boolean isSimulationStopped=false;
    }

    private final DataAboutCoronaCity dataAboutCoronaCity;
    City city;
    private static String clinic;
    private static String playButton;
    private static String house;
    public final Object locker = new Object();
    private final Object mapLocker = new Object();
    SimulationStopped simulationStopped=new SimulationStopped();



    public PageController(DataAboutCoronaCity dataAboutCoronaCity) {
        city = new City();
        this.dataAboutCoronaCity = dataAboutCoronaCity;
    }


    @FXML
    private GridPane MatrixWrapper;
    @FXML
    private GridPane map;
    @FXML
    private Button button;
    @FXML
    private ImageView allowMovementImageView;
    @FXML
    private ImageView sendAmbulanceImageView;
    @FXML
    private ImageView stopSimulationImageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        initImageViews();
        try {
            addHouses(dataAboutCoronaCity.getBrojKuca());
        } catch (NotAdultException | NotElderException | NotChildException e) {
            e.printStackTrace();
        }
        addControlStation(dataAboutCoronaCity.getKontrolniPunktovi());
        addRectangleToUnusedFieldsOfMatrix();
    }

    private void initImageViews() {
        allowMovementImageView.setOnMouseClicked(this::allowMovement);
        sendAmbulanceImageView.setOnMouseClicked(this::sendAmbulance);
    }

    //dodaju se ambulante u matricu grada
    public void initMap() {
        double gridWidth = 500;
        double gridHeight = 500;

        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;

        map.getColumnConstraints().clear();
        map.getRowConstraints().clear();
        for (int i = 0; i < city.getMatrix().length; i++) {
            map.getColumnConstraints().add(new ColumnConstraints(cellWidth));
            map.getRowConstraints().add(new RowConstraints(cellHeight));
        }
        //MatrixWrapper.add(map, 4, 2, 4, 4);
        //Image clinic=new Image(getClass().getResourceAsStream("clinic.png"));
        int numberOfResidents = dataAboutCoronaCity.getDjeca() + dataAboutCoronaCity.getStari() + dataAboutCoronaCity.getOdrasli();
        Random random = new Random();
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                if ((i == 0 && j == 0) || (i == 0 && j == (city.getMatrix().length - 1)) || (j == 0 && i == (city.getMatrix().length - 1)) || (i == (city.getMatrix().length - 1) && j == (city.getMatrix().length - 1))) {
                    Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                    Clinic clinic = new Clinic(10 / 100 * (numberOfResidents) + (random.nextInt() * (15 / 100 * numberOfResidents - 10 / 100 * numberOfResidents)));
                    rectangle.getStyleClass().add("rectangle-map");
                    rectangle.setFill(Color.rgb(238, 229, 222));
                    rectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                    rectangle.setUserData(clinic);
                    try {
                        city.setFieldOfMatrix(rectangle, i, j);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    map.add(rectangle, i, j);
                }

            }

        }
        System.out.println(dataAboutCoronaCity.getBrojKuca());
    }

    public void addHouses(int numberOfHouses) throws NotAdultException, NotElderException, NotChildException {
        Long[] arrayOfHouseIDs = new Long[dataAboutCoronaCity.getBrojKuca()];
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        Random r = new Random();
        int br = 0;
        while (br != numberOfHouses) {
            Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
            rectangle.getStyleClass().add("rectangle-map");
            rectangle.setFill(Color.rgb(238, 229, 222));
            //House house=new House((long)br);
            int iPosition = r.nextInt(city.getMatrix().length - 1);
            int jPosition = r.nextInt(city.getMatrix().length - 1);
            if ((Rectangle) city.getFieldOfMatrix(iPosition, jPosition) == null) {
                House house = new House(null);
                CityDataStore.getInstance().addHouse(house);
                rectangle.setUserData(house);
                rectangle.setFill(new ImagePattern(new Image("view/images/home.png")));
                city.setFieldOfMatrix(rectangle, iPosition, jPosition);
                map.add(rectangle, iPosition, jPosition);
                house.setFirstCoordinateOfHouse(iPosition);
                house.setSecondCoordinateOfHouse(jPosition);
                rectangle.setOnMouseClicked(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("x=" + house.getFirstCoordinateOfHouse() + ", y=" + house.getSecondCoordinateOfHouse() + ", name" + rectangle.getUserData().getClass().getName());
                    alert.show();
                });
                br++;
            }
        }
        //dodavanje stanovnika u kuce
        int numberOfHouseSafeForKids = 0;
        Random random = new Random();
        Long[] houseIDsSafeForKids = new Long[dataAboutCoronaCity.getOdrasli() + dataAboutCoronaCity.getStari()];
        List<House> houses = CityDataStore.getInstance().getHouses();
        for (int o = 0; o < dataAboutCoronaCity.getOdrasli(); o++) {
            int randomHouseIndex = random.nextInt(houses.size());
            House randomHouse = houses.get(randomHouseIndex);
            Long houseId = randomHouse.getId();
            houseIDsSafeForKids[numberOfHouseSafeForKids++] = houseId;
            int year = Calendar.getInstance().get(Calendar.YEAR) - (18 + new Random().nextInt(65 - 18));
            Gender gender = new Random().nextInt(100) < 50 ? Gender.Female : Gender.Male;
            Adult adult = new Adult(null, Resident.getNameRandomly(), Resident.getSurnameRandomly(), year, gender, houseId);
            adult.setCurrentPositionOfResident(randomHouse.getFirstCoordinateOfHouse(), randomHouse.getSecondCoordinateOfHouse());
            CityDataStore.getInstance().addResident(adult);

        }
        for (int s = 0; s < dataAboutCoronaCity.getStari(); s++) {
            int randomHouseIndex = random.nextInt(houses.size());
            House randomHouse = houses.get(randomHouseIndex);
            Long houseId = randomHouse.getId();
            houseIDsSafeForKids[numberOfHouseSafeForKids++] = houseId;
            int year = Calendar.getInstance().get(Calendar.YEAR) - (65 + new Random().nextInt(120 - 65));
            Gender gender = new Random().nextInt(100) < 50 ? Gender.Female : Gender.Male;
            Elder elder = new Elder(null, Resident.getNameRandomly(), Resident.getSurnameRandomly(), year, gender, houseId);
            CityDataStore.getInstance().addResident(elder);
            elder.getCurrentPositionOfResident().setFirstCoordinate(randomHouse.getFirstCoordinateOfHouse());
            elder.getCurrentPositionOfResident().setSecondCoordinate(randomHouse.getSecondCoordinateOfHouse());
        }
        for (int d = 0; d < dataAboutCoronaCity.getDjeca(); d++) {
            int index = random.nextInt(houseIDsSafeForKids.length);
            Long randomHouseId = houseIDsSafeForKids[index];
            House randomHouse = null;
            for (House h : houses) {
                if (h.getId() == randomHouseId) {
                    randomHouse = h;
                }
            }
            int year = Calendar.getInstance().get(Calendar.YEAR) - new Random().nextInt(18);
            Gender gender = new Random().nextInt(100) < 50 ? Gender.Female : Gender.Male;
            Child child = new Child(null, Resident.getNameRandomly(), Resident.getSurnameRandomly(), year, gender, houseIDsSafeForKids[index]);
            CityDataStore.getInstance().addResident(child);
            child.getCurrentPositionOfResident().setFirstCoordinate(randomHouse.getFirstCoordinateOfHouse());
            child.getCurrentPositionOfResident().setSecondCoordinate(randomHouse.getSecondCoordinateOfHouse());
        }

    }

    public void addControlStation(int controls) {
        double gridWidth = 500;

        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        Random r = new Random();
        int br = 0;
        //List<ControlStation>controlStations=CityDataStore.getInstance().getControlStations();
        while (br != controls) {
            Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
            rectangle.getStyleClass().add("rectangle-map");
            rectangle.setFill(Color.rgb(238, 229, 222));
            ControlStation controlStation = new ControlStation();
            int iPosition = r.nextInt(city.getMatrix().length - 1);
            int jPosition = r.nextInt(city.getMatrix().length - 1);
            if (city.getFieldOfMatrix(iPosition, jPosition) == null) {
                br++;
                controlStation.setFirstCoordinateOfControlStation(iPosition);
                controlStation.setSecondCoordinateOfControlStation(jPosition);
                rectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                rectangle.setUserData(controlStation);
                map.add(rectangle, iPosition, jPosition);
                city.setFieldOfMatrix(rectangle, iPosition, jPosition);
                CityDataStore.getInstance().addControlStation(controlStation);
            } else
                continue;

        }
    }

    public void addRectangleToUnusedFieldsOfMatrix() {
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                if ((Rectangle) city.getFieldOfMatrix(i, j) == null) {
                    Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                    rectangle.getStyleClass().add("rectangle-map");
                    rectangle.setFill(Color.rgb(238, 229, 222));
                    map.add(rectangle, i, j);
                    city.setFieldOfMatrix(rectangle, i, j);
                }
            }
        }
    }

    public void loadProperty() throws IOException {
        Properties properties = new Properties();
        String propertiesFileName = "view/properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
        if (inputStream != null) {
            properties.load(inputStream);
        }
        clinic = properties.getProperty("clinic");
        playButton = properties.getProperty("playButton");
        house = properties.getProperty("house");
    }

    @FXML
    private void allowMovement(MouseEvent e) {
        Thread t2 = new Thread(() -> {
            HashMap<Long, CurrentPositionOfResident> newCoordinates = new HashMap<>();
            boolean wasControlStationOnPreviousRectangle=false;
            Object o=null;
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                for (int resIndex = 0; resIndex < CityDataStore.getInstance().getResidents().size() && simulationStopped.isSimulationStopped==false; ) {
                    Resident r = CityDataStore.getInstance().getResidents().get(resIndex);
                    Direction direction = chooseDirectionOfMovement();
                    //if (r instanceof Child) {
                    int firstCoordinate = r.getCurrentPositionOfResident().getFirstCoordinate();
                    int secondCoordinate = r.getCurrentPositionOfResident().getSecondCoordinate();
                    Object field = city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                    Rectangle rectangle = (Rectangle) field;
                    Object fieldContent = rectangle.getUserData();
                    Object nextFieldContent;
                    Rectangle nextRectangle;
                    Object nextField;



                    Rectangle oldRectangle;

                    List<ControlStation>controlStations=CityDataStore.getInstance().getControlStations();

                        /*if (r instanceof Child) {
                            Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == r.getHouseID()).findFirst();
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
                                    nextFieldContent instanceof ControlStation ||
                                    nextFieldContent instanceof House ||
                                    nextFieldContent instanceof Resident) {
                                System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                                resIndex = 0;
                                continue;
                            }
                            else if(nextFieldContent instanceof ControlStation){
                                wasControlStationOnPreviousRectangle=false;
                            }
                                //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik
                            //synchronized (mapLocker) {
                            if (!(fieldContent instanceof House)) {
                                var newCords = newCoordinates.get(r.getId());
                                oldRectangle = (Rectangle) city.getFieldOfMatrix(newCords.getFirstCoordinate(), newCords.getSecondCoordinate());
                                oldRectangle.setFill(Color.rgb(238, 229, 222));
                                oldRectangle.setUserData(null);
                               for (int i = 0; i < CityDataStore.getInstance().getControlStations().size()-1 && !wasControlStationOnPreviousRectangle; i++) {
                                    if (((Resident) oldRectangle.getUserData()).getCurrentPositionOfResident().getFirstCoordinate() == controlStations.get(i).getFirstCoordinateOfControlStation()
                                            && ((Resident) oldRectangle.getUserData()).getCurrentPositionOfResident().getSecondCoordinate() == controlStations.get(i).getSecondCoordinateOfControlSttaion()) {
                                        oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                        oldRectangle.setUserData(new ControlStation());
                                        wasControlStationOnPreviousRectangle = true;
                                    } else {
                                        oldRectangle.setFill(Color.rgb(238, 229, 222));
                                        oldRectangle.setUserData(null);
                                    }

                                }
                                System.out.println("Stare pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                        r.getCurrentPositionOfResident().getSecondCoordinate());
                                //pozicija stanovnika u toku kretanja
                                newCoordinates.put(r.getId(), new CurrentPositionOfResident(firstCoordinate, secondCoordinate));
                                r.getCurrentPositionOfResident().setFirstCoordinate(firstCoordinate);
                                r.getCurrentPositionOfResident().setSecondCoordinate(secondCoordinate);
                                Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                               if (newRectangle.getUserData() instanceof ControlStation) {
                                    newRectangle.setFill(new ImagePattern(new Image("view/images/thermometer+child.png")));
                                } else {
                                    newRectangle.setFill(new ImagePattern(new Image("view/images/child.png")));
                               // }
                                newRectangle.setUserData(r);
                                //}
                                System.out.println("Nove pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                        r.getCurrentPositionOfResident().getSecondCoordinate());
                                System.out.println(r.getName() + r.getId() + ',' + direction +
                                        "(" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," + r.getCurrentPositionOfResident().getSecondCoordinate() + ")");
                            }
                        }*/
                      if(r instanceof Adult) {
                          Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == r.getHouseID()).findFirst();
                          if (!checkBoundsForChild( direction, firstCoordinate, secondCoordinate, city)) {
                              System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                              resIndex=0;
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
                              resIndex=0;
                              continue;
                          } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik


                          //synchronized (mapLocker) {
                          if (!(fieldContent instanceof House)) {
                              var newCords = newCoordinates.get(r.getId());
                              oldRectangle = (Rectangle) city.getFieldOfMatrix(newCords.getFirstCoordinate(), newCords.getSecondCoordinate());
                              if (o!=null) {
                                  for (int i = 0; i < CityDataStore.getInstance().getControlStations().size() - 1 && wasControlStationOnPreviousRectangle; i++) {
                                      if (((ControlStation) o).getFirstCoordinateOfControlStation() == controlStations.get(i).getFirstCoordinateOfControlStation()
                                              && ((ControlStation) o).getSecondCoordinateOfControlStation() == controlStations.get(i).getSecondCoordinateOfControlStation()) {
                                          oldRectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                                          oldRectangle.setUserData((ControlStation) o);
                                          //wasControlStationOnPreviousRectangle=false;

                                      }
                                  }

                              }
                                  if (!wasControlStationOnPreviousRectangle) {
                                      oldRectangle.setFill(Color.rgb(238, 229, 222));
                                      oldRectangle.setUserData(null);
                                      if (nextFieldContent instanceof ControlStation) {
                                          wasControlStationOnPreviousRectangle = true;
                                          o = nextFieldContent;
                                      }

                                  }
                                  else {
                                      o = null;
                                      wasControlStationOnPreviousRectangle = false;
                                  }

                          }



                          System.out.println("Stare pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                  r.getCurrentPositionOfResident().getSecondCoordinate());
                          //pozicija stanovnika u toku kretanja
                          newCoordinates.put(r.getId(), new CurrentPositionOfResident(firstCoordinate, secondCoordinate));
                          r.getCurrentPositionOfResident().setFirstCoordinate(firstCoordinate);
                          r.getCurrentPositionOfResident().setSecondCoordinate(secondCoordinate);
                          Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                          if (newRectangle.getUserData() instanceof ControlStation) {
                              newRectangle.setFill(new ImagePattern(new Image("view/images/thermometer+child.png")));
                          } else {
                              newRectangle.setFill(new ImagePattern(new Image("view/images/child.png")));
                               }
                              newRectangle.setUserData(r);

                          //}
                          System.out.println("Nove pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                  r.getCurrentPositionOfResident().getSecondCoordinate());
                          System.out.println(r.getName()+r.getId()+','+direction+
                                  "("+r.getCurrentPositionOfResident().getFirstCoordinate()+","+r.getCurrentPositionOfResident().getSecondCoordinate()+")");

                      }

                      else if (r instanceof Elder) {
                        Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == r.getHouseID()).findFirst();
                        if (!checkBoundsForElder(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                            System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                            resIndex=0;
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
                                nextFieldContent instanceof ControlStation ||
                                nextFieldContent instanceof House ||
                                nextFieldContent instanceof Resident) {
                            System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                            resIndex=0;
                            continue;
                        } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik

                        //synchronized (mapLocker) {
                        if (!(fieldContent instanceof House)) {
                            var newCords = newCoordinates.get(r.getId());
                            oldRectangle = (Rectangle) city.getFieldOfMatrix(newCords.getFirstCoordinate(), newCords.getSecondCoordinate());
                            oldRectangle.setFill(Color.rgb(238, 229, 222));
                            oldRectangle.setUserData(null);
                        }
                            if(!checkDistance(firstCoordinate,secondCoordinate,city,direction)) {
                                System.out.println("Distanca bi bila narusena");
                                resIndex=0;
                                continue;
                            }

                            System.out.println("Stare pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                r.getCurrentPositionOfResident().getSecondCoordinate());
                        //pozicija stanovnika u toku kretanja
                        newCoordinates.put(r.getId(), new CurrentPositionOfResident(firstCoordinate, secondCoordinate));
                        r.getCurrentPositionOfResident().setFirstCoordinate(firstCoordinate);
                        r.getCurrentPositionOfResident().setSecondCoordinate(secondCoordinate);
                        Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                        newRectangle.setFill(new ImagePattern(new Image("view/images/elder.png")));
                        newRectangle.setUserData(r);
                        //}
                        System.out.println("Nove pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                r.getCurrentPositionOfResident().getSecondCoordinate());
                            System.out.println(r.getName()+r.getId()+','+direction+
                                    "("+r.getCurrentPositionOfResident().getFirstCoordinate()+","+r.getCurrentPositionOfResident().getSecondCoordinate()+")");

                    }
                        else if (r instanceof Adult) {
                            Optional<House> optionalHouse = CityDataStore.getInstance().getHouses().stream().filter(h -> h.getId() == r.getHouseID()).findFirst();
                            if (!checkBoundsForAdult(optionalHouse.get(), direction, firstCoordinate, secondCoordinate, city)) {
                                System.out.println("Checking bounds: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                                resIndex=0;
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
                                    nextFieldContent instanceof ControlStation ||
                                    nextFieldContent instanceof House ||
                                    nextFieldContent instanceof Resident) {
                                System.out.println("Next field is filled: " + direction + ", " + firstCoordinate + ", " + secondCoordinate);
                                resIndex=0;
                                continue;
                            } //inace obrisi covjeka sa ruba matrice i na njegovo mjesto nacrtaj pravougaonik

                            //synchronized (mapLocker) {
                            if (!(fieldContent instanceof House)) {
                                var newCords = newCoordinates.get(r.getId());
                                oldRectangle = (Rectangle) city.getFieldOfMatrix(newCords.getFirstCoordinate(), newCords.getSecondCoordinate());
                                oldRectangle.setFill(Color.rgb(238, 229, 222));
                                oldRectangle.setUserData(null);
                            }
                            if(!checkDistance(firstCoordinate,secondCoordinate,city,direction)){
                                System.out.println("Distanca bi bila narusena");
                                resIndex=0;
                                continue;
                            }

                            System.out.println("Stare pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                    r.getCurrentPositionOfResident().getSecondCoordinate());
                            //pozicija stanovnika u toku kretanja
                            newCoordinates.put(r.getId(), new CurrentPositionOfResident(firstCoordinate, secondCoordinate));
                            r.getCurrentPositionOfResident().setFirstCoordinate(firstCoordinate);
                            r.getCurrentPositionOfResident().setSecondCoordinate(secondCoordinate);
                            Rectangle newRectangle = (Rectangle) city.getFieldOfMatrix(firstCoordinate, secondCoordinate);
                            newRectangle.setFill(new ImagePattern(new Image("view/images/adult.png")));
                            newRectangle.setUserData(r);
                            //}
                            System.out.println("Nove pozicije:" + r.getCurrentPositionOfResident().getFirstCoordinate() + "," +
                                    r.getCurrentPositionOfResident().getSecondCoordinate());
                            System.out.println(r.getName()+r.getId()+','+direction+
                                    "("+r.getCurrentPositionOfResident().getFirstCoordinate()+","+r.getCurrentPositionOfResident().getSecondCoordinate()+")");

                        }
                        resIndex++;
                    /*try {
                        Thread.sleep(5);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }*/
                    if(stopSimulationImageView.isPressed()){
                        synchronized (simulationStopped) {
                            simulationStopped.isSimulationStopped = true;
                        }

                    }

                }

                }
        });
        t2.start();
    }

    private Object getObjectNextToChoosedField(int firstCoordinate,int secondCoordinate){
        return ((Rectangle)city.getFieldOfMatrix(firstCoordinate,secondCoordinate)).getUserData();
    }

    private boolean checkDistance(int firstCoordinate, int secondCoordinate,City city,Direction direction) {
        //ako na sve 4 strane od izabranog polja na prvoj i drugoj poziciji nema objekta ili je on dijete,dopustice postavljanje objekta
         // if (direction == Direction.Up) {
            if ((secondCoordinate - 1) < 0)
                return true;
            else {
                if(getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 1) instanceof Elder)
                    return false;
            }
            if ((secondCoordinate - 2) < 0)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate, secondCoordinate - 2) instanceof Elder)
                    return false;
            }
       // } else if (direction == Direction.Bottom) {
            if ((secondCoordinate + 1) > city.getMatrix().length-1)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 1) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate, secondCoordinate +1) instanceof Elder)
                    return false;
            }
            if ((secondCoordinate + 2) > city.getMatrix().length-1)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate, secondCoordinate + 2) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate, secondCoordinate +2) instanceof Elder)
                    return false;
            }
      // } else if (direction == Direction.Left) {
            if ((firstCoordinate - 1) < 0)
                return true;
                else{

                  if(getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Adult
                          || getObjectNextToChoosedField(firstCoordinate - 1, secondCoordinate) instanceof Elder)
                      return false;
              }
            if ((firstCoordinate - 2) < 0)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate - 2, secondCoordinate) instanceof Elder)
                    return false;
            }
       // } else if (direction == Direction.Right) {
            if ((firstCoordinate + 1) > city.getMatrix().length-1)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate + 1, secondCoordinate) instanceof Elder)
                    return false;
            }
            if ((firstCoordinate + 2) > city.getMatrix().length-1)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate +2, secondCoordinate) instanceof Adult
                        || getObjectNextToChoosedField(firstCoordinate +2, secondCoordinate) instanceof Elder)
                    return false;
            }
            if(firstCoordinate-1<0 || secondCoordinate-1<0)
                return true;
            else{
                if(getObjectNextToChoosedField(firstCoordinate-1,secondCoordinate-1) instanceof Adult ||
                getObjectNextToChoosedField(firstCoordinate-1,secondCoordinate-1) instanceof Elder)
                    return false;
            }
        if(firstCoordinate-2<0 || secondCoordinate-2<0)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate-2,secondCoordinate-2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate-2,secondCoordinate-2) instanceof Elder)
                return false;
        }
        if(firstCoordinate-1<0 || secondCoordinate+1>city.getMatrix().length-1)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate-1,secondCoordinate+1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate-1,secondCoordinate+1) instanceof Elder)
                return false;
        }
        if(firstCoordinate-2<0 || secondCoordinate+2>city.getMatrix().length-1)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate-2,secondCoordinate+2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate-2,secondCoordinate+2) instanceof Elder)
                return false;
        }
        if(firstCoordinate+1>city.getMatrix().length-1 || secondCoordinate-1<0)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate+1,secondCoordinate-1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate+1,secondCoordinate-1) instanceof Elder)
                return false;
        }
        if(firstCoordinate+2>city.getMatrix().length-1 || secondCoordinate-2<0)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate+2,secondCoordinate-2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate+2,secondCoordinate-2) instanceof Elder)
                return false;
        }
        if(firstCoordinate+1>city.getMatrix().length-1 || secondCoordinate+1>city.getMatrix().length-1)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate+1,secondCoordinate+1) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate+1,secondCoordinate+1) instanceof Elder)
                return false;
        }
        if(firstCoordinate+2>city.getMatrix().length-1 || secondCoordinate+2>city.getMatrix().length-1)
            return true;
        else{
            if(getObjectNextToChoosedField(firstCoordinate+2,secondCoordinate+2) instanceof Adult ||
                    getObjectNextToChoosedField(firstCoordinate+2,secondCoordinate+2) instanceof Elder)
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
    private boolean checkBoundsForElder(House house,Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        System.out.println("House coordinates:"+house.getFirstCoordinateOfHouse()+","+house.getSecondCoordinateOfHouse());
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate>house.getSecondCoordinateOfHouse()-3);
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate>house.getFirstCoordinateOfHouse()-3);
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate<house.getFirstCoordinateOfHouse()+3);
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate<house.getSecondCoordinateOfHouse()+3);
            }
            default -> {
                return false;
            }
        }
    }
    private boolean checkBoundsForAdult(House house,Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        System.out.println("House coordinates:"+house.getFirstCoordinateOfHouse()+","+house.getSecondCoordinateOfHouse());
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate>house.getSecondCoordinateOfHouse()-Math.round(0.25*city.getMatrix().length));
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate>house.getFirstCoordinateOfHouse()-Math.round(0.25*city.getMatrix().length));
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate<house.getFirstCoordinateOfHouse()+Math.round(0.25*city.getMatrix().length));
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate<house.getSecondCoordinateOfHouse()+Math.round(0.25*city.getMatrix().length));
            }
            default -> {
                return false;
            }
        }
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

    @FXML
    private void sendAmbulance(MouseEvent e) {
        Thread thread = new Thread(() -> {
            try {
                synchronized (locker) {

                    locker.wait();
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Poslano je ambulanto vozilo. 😊");
                a.show();
            });

        });
        thread.start();
    }

    @FXML void stopSimulation(MouseEvent e){
        System.out.println("Simulacija zavrsena..");
        System.exit(0);



    }

}
