package controller;

import components.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.JavaFXUtil;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


public class PageController implements Initializable {


    private Alarm alarm;
    private final Object lockerAddingToClinics=new Object();

    public class SimulationStopped {
        public boolean isSimulationStopped = false;
    }

    private final DataAboutCoronaCity dataAboutCoronaCity;
    City city;
    private static String clinic;
    private static String playButton;
    private static String house;
    private final Object mapLocker = new Object();
    public static final Object lockerInfectedPerson = new Object();
    public static final Object lockerThreadRunning = new Object();
    SimulationStopped simulationStopped = new SimulationStopped();
    public static List<Thread> listOfActiveThreads = new ArrayList<>();
    public static Thread residentThread;
    public static boolean isThreadRunning = true;
    List<Alarm> alarms = new ArrayList<>();

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
    @FXML
    private ImageView runAgainImageView;
    @FXML
    private Text infectedPatients;
    @FXML
    private Text recoveredPatients;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane notificationScrollPane;

    public static  Handler handler;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

        File file1=new File("dataAboutMovement.txt");
        file1.delete();
        File file2=new File("userNotifications.txt");
        file2.delete();
        initMap();
        initImageViews();
        try {
            addHouses(dataAboutCoronaCity.getBrojKuca());
        } catch (NotAdultException | NotElderException | NotChildException e) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,e.fillInStackTrace().toString());
        }
        addControlStation(dataAboutCoronaCity.getKontrolniPunktovi());
        addRectangleToUnusedFieldsOfMatrix();
        for(int i=0;i<dataAboutCoronaCity.getAmbulantnaVozila();i++){
            CityDataStore.getInstance().getAmbulances().add(new Ambulance());
        }
    }

    private void initImageViews() {
        allowMovementImageView.setOnMouseClicked(this::allowMovement);
        runAgainImageView.setOnMouseClicked(this::startSimulationAgain);
        //sendAmbulanceImageView.setOnMouseClicked(this::sendAmbulance);
    }

    //dodaju se ambulante u matricu grada
    public void initMap() {
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

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
                    //int clinicCapacity = (int) (10.0 / 100 * (numberOfResidents) + (random.nextDouble() * (5.0 / 100 * numberOfResidents)));
                    int clinicCapacity = 4;
                    int id = random.nextInt(100);
                    System.out.println("Kapacitet novokreirane klinike je " + clinicCapacity);
                    Clinic clinic = new Clinic(id,clinicCapacity, i, j);
                    rectangle.getStyleClass().add("rectangle-map");
                    rectangle.setFill(Color.rgb(238, 229, 222));
                    rectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                    rectangle.setUserData(clinic);
                    int finalI = i;
                    int finalJ = j;
                    rectangle.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        StringBuilder content = new StringBuilder("x=" + finalI + ", y=" + finalJ + ", name" );
                        content.append(System.lineSeparator());
                        List<Resident> residents = CityDataStore.getInstance()
                                .getResidents()
                                .stream()
                                .filter(res -> res.getCurrentPositionOfResident().getFirstCoordinate() == finalI &&
                                        res.getCurrentPositionOfResident().getSecondCoordinate() == finalJ)
                                .collect(Collectors.toList());
                        for (Resident res : residents) {
                            content.append(res.getId()).append(",");
                        }
                        alert.setContentText(content.toString());
                        alert.show();
                    });
                    CityDataStore.getInstance().addClinic(clinic);
                    try {
                        city.setFieldOfMatrix(rectangle, i, j);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING,e.fillInStackTrace().toString());
                    }
                    map.add(rectangle, i, j);
                }

            }

        }
    }

    public void addHouses(int numberOfHouses) throws NotAdultException, NotElderException, NotChildException {
        Long[] arrayOfHouseIDs = new Long[dataAboutCoronaCity.getBrojKuca()];
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        Random r = new Random();
        int br = 0;

        List<PositionOfResident> freePositions = new ArrayList<>();
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                if (city.getMatrix()[i][j] == null) {
                    freePositions.add(new PositionOfResident(i, j));
                }
            }
        }
        while (br != numberOfHouses) {
            Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
            rectangle.getStyleClass().add("rectangle-map");
            rectangle.setFill(Color.rgb(238, 229, 222));
            //House house=new House((long)br);

            int freeIndex = r.nextInt(freePositions.size() - 1);
            PositionOfResident freePosition = freePositions.get(freeIndex);
            int iPosition = freePosition.getFirstCoordinate();
            int jPosition = freePosition.getSecondCoordinate();
            if ((Rectangle) city.getFieldOfMatrix(iPosition, jPosition) == null && city.checkDistanceOfField(iPosition, jPosition, null,0, House.class)) {
                freePositions.remove(freeIndex);
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
                    StringBuilder content = new StringBuilder("x=" + house.getFirstCoordinateOfHouse() + ", y=" + house.getSecondCoordinateOfHouse() + ", name" );
                    content.append(System.lineSeparator());
                    List<Resident> residents = CityDataStore.getInstance()
                            .getResidents()
                            .stream()
                            .filter(res -> res.getCurrentPositionOfResident().getFirstCoordinate() == house.getFirstCoordinateOfHouse() &&
                                    res.getCurrentPositionOfResident().getSecondCoordinate() == house.getSecondCoordinateOfHouse())
                            .collect(Collectors.toList());
                    for (Resident res : residents) {
                        content.append(res.getId()).append(",");
                    }
                    alert.setContentText(content.toString());
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
            ComponentsCityDataStore.getInstance().addResident(new AdultComponent(adult));

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
            ComponentsCityDataStore.getInstance().addResident(new ElderComponent(elder));
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
            ComponentsCityDataStore.getInstance().addResident(new ChildComponent(child));
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
                    int finalI = i;
                    int finalJ = j;
                    rectangle.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        StringBuilder content = new StringBuilder("x=" + finalI + ", y=" + finalJ + ", name" );
                        content.append(System.lineSeparator());
                        List<Resident> residents = CityDataStore.getInstance()
                                .getResidents()
                                .stream()
                                .filter(res -> res.getCurrentPositionOfResident().getFirstCoordinate() == finalI &&
                                        res.getCurrentPositionOfResident().getSecondCoordinate() == finalJ)
                                .collect(Collectors.toList());
                        for (Resident res : residents) {
                            content.append(res.getId()).append(",");
                        }
                        alert.setContentText(content.toString());
                        alert.show();
                    });
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
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

        CityDataStore.getInstance().setStartTimeOfSimulation(System.currentTimeMillis());
        Thread t2 = new Thread(() -> {

            List<ResidentComponent> residentsComponents = ComponentsCityDataStore.getInstance().getResidents();
            for (ResidentComponent resident : residentsComponents) {
                resident.setDataAboutCoronaCity(dataAboutCoronaCity);
                resident.setSimulationStopped(simulationStopped);
                resident.setCity(city);
                residentThread = new Thread(resident);
                listOfActiveThreads.add(residentThread);
                residentThread.start();
            }
            for (Thread thread : listOfActiveThreads) {
                try {
                    thread.join();
                } catch (InterruptedException interruptedException) {
//                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Failure.");
//                    alert.show();
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
                }
            }
//            Alert alert = new Alert(Alert.AlertType.INFORMATION, "END :D");
//            alert.showAndWait();
        });
        t2.start();
        Thread t3 = new Thread(() -> {
            Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
                }
                while (!ResidentComponent.stackOfAlarms.empty()) {
                    alarm = ResidentComponent.stackOfAlarms.pop();
                    alarms.add(alarm);
                    File file=new File("userNotifications.txt");
                    try{
                        if(!file.exists()){
                            file.createNewFile();
                        }

                        FileWriter fileWriter=new FileWriter(file,true);

                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write("Posaljite ambulantu na poziciju (" + alarm.getFirstCoordinate() + "," + alarm.getSecondCoordinate() + ").");
                        bufferedWriter.write("\r\n");
                        bufferedWriter.close();

                    } catch(IOException ex) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ex.fillInStackTrace().toString());
                    }
                    try {
                        BufferedReader br=new BufferedReader(new FileReader("userNotifications.txt"));
                        String content=br.readLine();
                        String s="";
                        while(content!=null) {
                            s+= content+"\r\n";
                            content=br.readLine();
                        }
                        Text text=new Text();
                        text.setText(s);
                        Platform.runLater(()->notificationScrollPane.setContent(text));
                    } catch (IOException exception) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING,exception.fillInStackTrace().toString());
                    }
                   /* Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.INFORMATION);
                        a.setContentText("Posaljite ambulantu na poziciju (" + alarm.getFirstCoordinate() + "," + alarm.getSecondCoordinate() + ").");
                        a.show();
                    });*/

                }
            }
        });
        t3.start();
        Thread thread1 = new Thread(() -> {
            Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
                }
                for (Clinic clinic : CityDataStore.getInstance().getClinics()) {
                    clinic.removeRecoveredResident();
                    detectChangeOfFile();
                }
            }
        });

        thread1.start();
    }

    @FXML
    private void sendAmbulance(MouseEvent e) {
        Thread thread = new Thread(() -> {
            Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
//            try {
//                synchronized (mapLocker) {
//
//                    mapLocker.wait();
//                }
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }

            /*Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Poslano je ambulanto vozilo. 😊");
                a.show();

            });*/
            boolean isAdded = false;
            for (int a = 0, size = CityDataStore.getInstance().getAmbulances().size(); a < size && !isAdded; a++) {
                if (CityDataStore.getInstance().getAmbulances().get(a).getAmbulanceFree()) {
                    List<Clinic> clinics = CityDataStore.getInstance().getClinics();
                    for (int i = 0, clinicsSize = clinics.size(); i < clinicsSize && !isAdded; i++) {
                        Clinic clinic = clinics.get(i);
                        for (int j = 0; j < alarms.size() && !isAdded; j++) {
                            Alarm alarm = alarms.get(j);
                            if (clinic.addInfectedResident(alarm.getResident())) {
                                CityDataStore.getInstance().getAmbulances().get(a).setAmbulanceFree(false);
                                isAdded = true;
                                alarms.remove(j);
                                File file=new File("userNotifications.txt");
                                try{
                                    if(!file.exists()){
                                        file.createNewFile();
                                    }

                                    FileWriter fileWriter=new FileWriter(file,true);

                                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                    bufferedWriter.write("Ambulantno vozilo poslano i stanovnik " + alarm.getResident().getId() + " je dodan u kliniku " + clinic.getCapacityOfClinic());
                                    bufferedWriter.write("\r\n");
                                    bufferedWriter.close();

                                } catch(IOException ex) {
                                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ex.fillInStackTrace().toString());
                                }
                                try {
                                    BufferedReader br=new BufferedReader(new FileReader("userNotifications.txt"));
                                    String content=br.readLine();
                                    String s="";
                                    while(content!=null) {
                                        s+= content+"\r\n";
                                        content=br.readLine();
                                    }
                                    Text text=new Text();
                                    text.setText(s);
                                    Platform.runLater(()->notificationScrollPane.setContent(text));
                                } catch (IOException exception) {
                                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,exception.fillInStackTrace().toString());
                                }
                                //System.out.println("Poslano");
                                //System.out.println("stanovnik " + alarm.getResident().getId() + " je dodan u kliniku " + clinic.getCapacityOfClinic());
                                Random random=new Random();
                                try {
                                    Thread.sleep(random.nextInt(5000));
                                } catch (InterruptedException interruptedException) {
                                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
                                }
                                CityDataStore.getInstance().getAmbulances().get(a).setAmbulanceFree(true);
                            } else {
                                if (i == clinicsSize - 1) {
                                    System.out.println("Kapaciteti klinika su popunjeni.Kreirajte novu kliniku.");
                                    // TODO: Napraviti čekanje nove klinike
                                }
                            }
                        }
                    }
                }
            }
            if(!isAdded) {
                File file=new File("userNotifications.txt");
                try{
                    if(!file.exists()){
                        file.createNewFile();
                    }

                    FileWriter fileWriter=new FileWriter(file,true);

                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("Trenutno nema slobodnog vozila.Pokusajte ponovo kasnije...");
                    bufferedWriter.write("\r\n");
                    bufferedWriter.close();

                } catch(IOException ex) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ex.fillInStackTrace().toString());
                }
                try {
                    BufferedReader br=new BufferedReader(new FileReader("userNotifications.txt"));
                    String content=br.readLine();
                    String s="";
                    while(content!=null) {
                        s+= content+"\r\n";
                        content=br.readLine();
                    }
                    Text text=new Text();
                    text.setText(s);
                    Platform.runLater(()->notificationScrollPane.setContent(text));
                } catch (IOException exception) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,exception.fillInStackTrace().toString());
                }
                //System.out.println("Trenutno nema slobodnog vozila.Pokusajte ponovo kasnije...");
            }
            synchronized (lockerInfectedPerson) {
                synchronized (lockerThreadRunning) {
                    //isThreadRunning = false;//da zaustavimo tred zarazenog stanovnika
                }
                lockerInfectedPerson.notify();
            }
        });
        thread.start();
        }

    @FXML
    void stopSimulation(MouseEvent e) {
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

        CityDataStore.getInstance().setEndTimeOfSimulation(System.currentTimeMillis());
        PrintWriter endOfSimulation = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH_mm_ss__dd_MM_yyyy");
        LocalDateTime now = LocalDateTime.now();
        try {
            endOfSimulation = new PrintWriter(new BufferedWriter(new FileWriter("SIM-JavaKov-20-"+dtf.format(now)+".txt")));
            endOfSimulation.println("Trajanje simulacije: "+(CityDataStore.getInstance().getEndTimeOfSimulation()-CityDataStore.getInstance().getStartTimeOfSimulation())/1000+" sekundi");
            endOfSimulation.println();
            endOfSimulation.println("Broj kreiranih objekata:");
            endOfSimulation.println("  -djeca: "+dataAboutCoronaCity.getDjeca());
            endOfSimulation.println("  -odrasli: "+dataAboutCoronaCity.getOdrasli());
            endOfSimulation.println("  -stari: "+dataAboutCoronaCity.getStari());
            endOfSimulation.println("  -kuce: "+dataAboutCoronaCity.getBrojKuca());
            endOfSimulation.println("  -kontrolni punktovi: "+dataAboutCoronaCity.getKontrolniPunktovi());
            endOfSimulation.println("  -ambulantna vozila: "+dataAboutCoronaCity.getAmbulantnaVozila());
            endOfSimulation.println();
            endOfSimulation.println("Statisticki podaci:");
            endOfSimulation.println("  Zarazeni: "+CityDataStore.getInstance().getInfectedResidents().size()+" od cega:");
            endOfSimulation.println("    -djeca: "+CityDataStore.getInstance().getInfectedResidents()
                    .stream().filter(r->r instanceof Child).collect(Collectors.toList()).size());
            endOfSimulation.println("    -odrasli: "+CityDataStore.getInstance().getInfectedResidents()
                    .stream().filter(r->r instanceof Adult).collect(Collectors.toList()).size());
            endOfSimulation.println("    -stari: "+CityDataStore.getInstance().getInfectedResidents()
                    .stream().filter(r->r instanceof Elder).collect(Collectors.toList()).size());
            endOfSimulation.println("  Oporavljeni: "+CityDataStore.getInstance().getRecoveredResidents().size());
            endOfSimulation.close();
        } catch (IOException ioException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ioException.fillInStackTrace().toString());
        }
        System.out.println("Simulacija zavrsena i trajala je "+(CityDataStore.getInstance().getEndTimeOfSimulation()-CityDataStore.getInstance().getStartTimeOfSimulation())/1000);
        System.exit(0);
    }

    @FXML
    void reviewStateOfClinics(MouseEvent e) {
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

        Scene previousScene=allowMovementImageView.getScene();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/clinic_page.fxml"));
        StateOfClinicsController stateOfClinicsController=new StateOfClinicsController(previousScene,city);
        loader.setController(stateOfClinicsController);
        try {
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            //Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Stage stage=new Stage();
            stage.setScene(scene);
            stage.show();

        } catch (IOException ioException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ioException.fillInStackTrace().toString());
        }
        //new Thread(() -> {
           // table.setItems((ObservableList<Clinic>) CityDataStore.getInstance().getClinics());
            String s1="";
            String s2="";
            for (Clinic clinic : CityDataStore.getInstance().getClinics()) {
                s1+="Klinika"+clinic.getID()+"\n";
                s2+=clinic.getCapacityOfClinic()+"\n";
            }
            stateOfClinicsController.setTextNameContent(s1);
            stateOfClinicsController.setTextCapacityContent(s2);
        //}
        //).start();

    }

    @FXML
    void pauseSimulation(MouseEvent e) {
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
        simulationStopped.isSimulationStopped = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException interruptedException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
        }
        File file = new File("states");
        file.mkdirs();
        file = new File("states/" + System.currentTimeMillis() + ".ser");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(CityDataStore.getInstance());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Kretanje pauzirano i izvrsena serijalizacija.");
                    alert.showAndWait();
                });
            } catch (IOException ioException) {
                Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ioException.fillInStackTrace().toString());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Greška pri upisivanju u fajl.");
                    alert.showAndWait();
                });
            }
        } catch (IOException fileNotFoundException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,fileNotFoundException.fillInStackTrace().toString());
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Greška, fajl ne postoji.");
                alert.showAndWait();
            });
        }
    }
    @FXML
    private void showStatistic(MouseEvent e){
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);

        Scene previousScene=allowMovementImageView.getScene();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/statistics.fxml"));
        StatisticController statisticController=new StatisticController(previousScene);
        loader.setController(statisticController);
        try {
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            //Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Stage stage=new Stage();
            stage.setTitle("Corona City-statistic");
            stage.setScene(scene);
            stage.show();
            statisticController.addInPieChartNumber();
            statisticController.addInPieChartType();
            statisticController.addInPieChartGender();
        } catch (IOException ioException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ioException.fillInStackTrace().toString());
        }
    }

    void startSimulationAgain(MouseEvent e) {
        Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
        System.out.println("start sim again.");
        new Thread(() -> {
            Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
            simulationStopped.isSimulationStopped = true;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException interruptedException) {
                Logger.getLogger(PageController.class.getName()).log(Level.WARNING,interruptedException.fillInStackTrace().toString());
            }
            FileChooser.ExtensionFilter serFilter
                    = new FileChooser.ExtensionFilter("Serializabled files", "*.ser");

            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(serFilter);

            File initial = new File("states/");
            initial.mkdir();
            fc.setInitialDirectory(initial);
            var wrapper = new Object() {
                File file = null;
            };
            JavaFXUtil.runAndWait(() -> wrapper.file = fc.showOpenDialog(runAgainImageView.getScene().getWindow()));

            try (FileInputStream fis = new FileInputStream(wrapper.file)) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    try {
                        CityDataStore.getInstance().loadData((CityDataStore) ois.readObject());
                    } catch (ClassNotFoundException classNotFoundException) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING,classNotFoundException.fillInStackTrace().toString());
                    }
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Kretanje nastavljeno i izvrsena deserijalizacija.");
                        alert.showAndWait();
                    });
                } catch (IOException ioException) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ioException.fillInStackTrace().toString());
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Greška pri upisivanju u fajl.");
                        alert.showAndWait();
                    });
                }
            } catch (IOException fileNotFoundException) {
                Logger.getLogger(PageController.class.getName()).log(Level.WARNING,fileNotFoundException.fillInStackTrace().toString());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Greška, fajl ne postoji.");
                    alert.showAndWait();
                });
            }
            ComponentsCityDataStore.getInstance().setResidents(CityDataStore
                    .getInstance()
                    .getResidents()
                    .stream()
                    .map(Resident::mapToComponent)
                    .collect(Collectors.toList()));
            double gridWidth = 500;
            double gridHeight = 500;

            city.setCitySize(CityDataStore.getInstance().getCitySize());
            city.setMatrix(CityDataStore.getInstance().getCitySize());

            double cellHeight = gridHeight / city.getMatrix().length;
            double cellWidth = gridWidth / city.getMatrix().length;

            map.getColumnConstraints().clear();
            map.getRowConstraints().clear();
            Platform.runLater(()->map.getChildren().clear());

            for (int i = 0; i < city.getMatrix().length; i++) {
                map.getColumnConstraints().add(new ColumnConstraints(cellWidth));
                map.getRowConstraints().add(new RowConstraints(cellHeight));
            }

            for (int i = 0; i < CityDataStore.getInstance().getCitySize(); i++) {
                for (int j = 0; j < CityDataStore.getInstance().getCitySize(); j++) {
                    Rectangle rect=new Rectangle(cellHeight,cellWidth);
                    //Rectangle rect = (Rectangle) city.getMatrix()[i][j];
                    rect.getStyleClass().add("rectangle-map");
                    JavaFXUtil.runAndWait(() -> rect.setFill(Color.rgb(238, 229, 222)));
                    rect.setUserData(null);
                    city.setFieldOfMatrix(rect,i,j);
                    int finalI = i;
                    int finalJ = j;
                    Platform.runLater(() -> map.add(rect, finalI, finalJ));
                }
            }
            for (var residentComponent : ComponentsCityDataStore.getInstance().getResidents()) {
                var position = residentComponent.getResident().getCurrentPositionOfResident();
                Rectangle rect = (Rectangle) city.getMatrix()[position.getFirstCoordinate()][position.getSecondCoordinate()];
                JavaFXUtil.runAndWait(() -> rect.setFill(new ImagePattern(residentComponent.getImageOfResident())));
                rect.setUserData(residentComponent);
            }
            for (var house : CityDataStore.getInstance().getHouses()) {
                Rectangle rect = (Rectangle) city.getMatrix()[house.getFirstCoordinateOfHouse()][house.getSecondCoordinateOfHouse()];
                if (!(rect.getUserData() instanceof ResidentComponent)) {
                    rect.setUserData(house);
                }
                JavaFXUtil.runAndWait(() -> rect.setFill(new ImagePattern(new Image("view/images/home.png"))));
            }
            for (var clinic : CityDataStore.getInstance().getClinics()) {
                Rectangle rect = (Rectangle) city.getMatrix()[clinic.getFirstCoordinate()][clinic.getSecondCoordinate()];
                JavaFXUtil.runAndWait(() -> rect.setFill(new ImagePattern(new Image("view/images/clinic.png"))));
                rect.setUserData(clinic);
            }
            for (var controlStation : CityDataStore.getInstance().getControlStations()) {
                Rectangle rect = (Rectangle) city.getMatrix()[controlStation.getFirstCoordinateOfControlStation()][controlStation.getSecondCoordinateOfControlStation()];
                if (rect.getUserData() instanceof ResidentComponent) {
                    JavaFXUtil.runAndWait(() -> rect.setFill(new ImagePattern(((ResidentComponent) rect.getUserData()).getImageOfResidentWithThermometer())));
                } else {
                    JavaFXUtil.runAndWait(() -> rect.setFill(new ImagePattern(new Image("view/images/thermometer.png"))));
                    rect.setUserData(controlStation);
                }
            }
            simulationStopped.isSimulationStopped = false;
            allowMovement(e);

        }).start();
    }
    public  PageController getPageController(){
        return this;
    }
    private void detectChangeOfFile(){
        new Thread(()->{
            Logger.getLogger(PageController.class.getName()).addHandler(MainPageController.handler);
            WatchService watcher = null;
            try {
                watcher = FileSystems.getDefault().newWatchService();
                Path dir = Paths.get("C:\\Users\\goran\\Desktop\\PJ2_Projekat");
                dir.register(watcher, ENTRY_MODIFY);
                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PageController.class.getName()).log(Level.WARNING,ex.fillInStackTrace().toString());
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();
                        if(fileName.toString().trim().equals("clinic-info.txt")) {
                            List<String> content = Files.readAllLines(dir.resolve(fileName));
                            if(content.size()>0)
                                infectedPatients.setText(content.get(0));
                                if(content.size()>1)
                                recoveredPatients.setText(content.get(1));
                            //clinicScrollPane.setFitToWidth(true);
                            //Platform.runLater(()-> {
                            //    clinicScrollPane.setContent(infectedPatients);
                            //});
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }

            }
            catch (IOException e) {
                Logger.getLogger(PageController.class.getName()).log(Level.WARNING,e.fillInStackTrace().toString());
            }

        }).start();
    }
    public  ScrollPane getScrollPane(){
        return scrollPane;
    }
}
