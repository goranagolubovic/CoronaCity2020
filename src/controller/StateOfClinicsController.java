package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.City;
import model.CityDataStore;
import model.Clinic;

import javafx.scene.input.MouseEvent;
import java.util.Random;

public class StateOfClinicsController {

    private Scene previousScene;
    private City city;
    private boolean isClinicAdd=false;

    public StateOfClinicsController(Scene previousScene, City city){
        this.previousScene=previousScene;
        this.city=city;
    }
    @FXML
    private Text textName;
    @FXML
    private Text textCapacity;

    public StateOfClinicsController() {

    }

    public void setTextNameContent(String s){
        textName.setText(s);
    }
    public void setTextCapacityContent(String s){
        textCapacity.setText(s);
    }
        @FXML
         void createANewClinic(MouseEvent e) {
        System.out.println(city.getMatrix().length);
        double gridWidth = 500;
        double gridHeight = 500;

        double cellHeight = gridHeight / city.getMatrix().length;
        double cellWidth = gridWidth / city.getMatrix().length;

        Random r = new Random();
        while (!isClinicAdd) {
            int i = r.nextInt(city.getMatrix().length);
            int j;
            if (i == 0 || i == city.getMatrix().length) {
                //ako je prvi ili posljednji red onda j moze biti bilo koji broj sem 0  i posljednja kolona jer tu vec ima klinika
                j = r.nextInt(city.getMatrix().length-2)+1;
            } else {
                //inace j moze biti 0 ili posljednja kolona
                int temp = r.nextInt(2);
                if (temp == 1) {
                    j = 0;
                } else {
                    j = city.getMatrix().length-1;
                }
            }
            Rectangle rectangle=(Rectangle) city.getFieldOfMatrix(j,i);
            if(rectangle.getUserData()==null){
                Clinic clinic=new Clinic(r.nextInt(100),4,j,i);
                rectangle.setUserData(clinic);
                rectangle.setFill(new ImagePattern(new Image("view/images/clinic.png")));
                CityDataStore.getInstance().addClinic(clinic);
                isClinicAdd=true;
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(previousScene);
                stage.show();
            }

        }
    }


    public Scene getScene() {
        return previousScene;
    }
}
