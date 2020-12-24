package controller;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class PageController implements Initializable{

    private final DataAboutCoronaCity dataAboutCoronaCity;
    City city;
    private static String clinic;
    private static String playButton;
    private static String house;
    public Object locker = new Object();


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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        try {
            addHouses(dataAboutCoronaCity.getBrojKuca());
        } catch (NotAdultException | NotElderException | NotChildException e) {
            e.printStackTrace();
        }
        addControlStation(dataAboutCoronaCity.getKontrolniPunktovi());
        addRectangleToUnusedFieldsOfMatrix();
        //kretanje objekata
        Line line=new Line(2,2,2,2);
        PathTransition transition=new PathTransition();
        transition.setNode(button);
        transition.setPath(line);
        transition.setCycleCount(PathTransition.INDEFINITE);
        transition.play();
    }
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
        int numberOfResidents=dataAboutCoronaCity.getDjeca()+dataAboutCoronaCity.getStari()+dataAboutCoronaCity.getOdrasli();
        Random random=new Random();
        for (int i = 0; i < city.getMatrix().length; i++) {
            for (int j = 0; j < city.getMatrix().length; j++) {
                if ((i == 0 && j == 0) || (i == 0 && j == (city.getMatrix().length - 1)) || (j == 0 && i == (city.getMatrix().length - 1)) || (i == (city.getMatrix().length - 1) && j == (city.getMatrix().length - 1))) {
                    Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                    Clinic clinic=new Clinic(10/100*(numberOfResidents)+(random.nextInt()*(15/100*numberOfResidents-10/100*numberOfResidents)));
                    rectangle.getStyleClass().add("rectangle-map");
                    rectangle.setFill(Color.rgb(238, 229, 222));
                    rectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                    city.setFieldOfMatrix(rectangle,i,j);
                    map.add(rectangle, i, j);
                }

            }

        }
        System.out.println(dataAboutCoronaCity.getBrojKuca());
    }
    public void addHouses(int numberOfHouses) throws NotAdultException, NotElderException, NotChildException {
        Long arrayOfHouseIDs[]=new Long[dataAboutCoronaCity.getBrojKuca()];
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        Random r = new Random();
        int br=0;
        while(br!=numberOfHouses)
        {   Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
            rectangle.getStyleClass().add("rectangle-map");
            rectangle.setFill(Color.rgb(238, 229, 222));
            //House house=new House((long)br);
            int iPosition = r.nextInt(city.getMatrix().length-1);
            int jPosition = r.nextInt(city.getMatrix().length-1);
            if((Rectangle)city.getFieldOfMatrix(iPosition,jPosition)==null){
                House house=new House((long)br);
                arrayOfHouseIDs[br]=house.getHouseId();
                br++;
                rectangle.setUserData(house);
                rectangle.setFill(new ImagePattern(new Image("view/images/home.png")));
                city.setFieldOfMatrix(rectangle,iPosition,jPosition);
                map.add(rectangle,iPosition,jPosition);

            }
            else
                continue;

        }
        //dodavanje stanovnika u kuce
         int numberOfHouseSafeForKids=0;
        Random random=new Random();
        Long houseId;
        Long houseIDsSafeForKids[]=new Long[dataAboutCoronaCity.getOdrasli()+dataAboutCoronaCity.getStari()];
        for(int o=0;o<dataAboutCoronaCity.getOdrasli();o++) {
           houseId = arrayOfHouseIDs.length * new Random().nextLong();
            houseIDsSafeForKids[numberOfHouseSafeForKids++] = houseId;
            Resident.setAdults(100 * random.nextLong(), houseId);
        }
        for(int s=0;s<dataAboutCoronaCity.getStari();s++) {
            houseId = arrayOfHouseIDs.length * new Random().nextLong();
            houseIDsSafeForKids[numberOfHouseSafeForKids++] = houseId;
            Resident.setElders(100 * random.nextLong(), houseId);
        }
        for(int d=0;d<dataAboutCoronaCity.getDjeca();d++){
            int index=random.nextInt(houseIDsSafeForKids.length-1);
            Resident.setChildren(100*random.nextLong(),houseIDsSafeForKids[numberOfHouseSafeForKids-1]);
        }

    }
    public void addControlStation(int controls){
        double gridWidth = 500;

        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        Random r = new Random();
        int br=0;
        while(br!=controls)
        {   Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
            rectangle.getStyleClass().add("rectangle-map");
            rectangle.setFill(Color.rgb(238, 229, 222));
            ControlStation controlStation=new ControlStation();
            int iPosition = r.nextInt(city.getMatrix().length-1);
            int jPosition = r.nextInt(city.getMatrix().length-1);
            if(city.getFieldOfMatrix(iPosition,jPosition)==null){
                br++;
                rectangle.setFill(new ImagePattern(new Image("view/images/thermometer.png")));
                map.add(rectangle,iPosition,jPosition);
                city.setFieldOfMatrix(rectangle,iPosition,jPosition);
            }
            else
                continue;

        }
    }
    public void addRectangleToUnusedFieldsOfMatrix(){
        double gridWidth = 500;
        double gridHeight = 500;
        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;
        for(int i=0;i<city.getMatrix().length;i++){
            for(int j=0;j<city.getMatrix().length;j++){
                if((Rectangle)city.getFieldOfMatrix(i,j)==null){
                    Rectangle rectangle = new Rectangle(cellHeight, cellWidth);
                    rectangle.getStyleClass().add("rectangle-map");
                    rectangle.setFill(Color.rgb(238, 229, 222));
                    map.add(rectangle,i,j);
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
        house=properties.getProperty("house");
    }

    @FXML
    private void allowMovement(MouseEvent e) {
        Thread thread = new Thread(() -> {
            synchronized (locker) {

                locker.notify();
            }


            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Kretanje stanovnika je počelo. 😊");
                a.show();
            });
        });
        thread.start();
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

}
