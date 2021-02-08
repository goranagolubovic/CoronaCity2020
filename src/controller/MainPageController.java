package controller;

import components.ComponentsCityDataStore;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.CityDataStore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainPageController implements Initializable {

    public Object locker = new Object();
    public static Handler handler;
    protected static int brojKuca;
    @FXML
    private TextField kuce;
    @FXML
    private TextField ambulante;
    @FXML
    private TextField punktovi;
    @FXML
    private TextField odrasli;
    @FXML
    private TextField djeca;
    @FXML
    private TextField stari;
    @FXML
    private ImageView coronaVirusRotateImageView1;
    @FXML
    private ImageView coronaVirusRotateImageView2;
    @FXML
    private ImageView coronaVirusRotateImageView3;
    @FXML
    private ImageView coronaVirusRotateImageView4;

    public MainPageController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startRotate();
        //kreiraj log fajl
        File logFile=new File("corona_city.log");
        if(logFile.exists()){
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startSimulation(ActionEvent e) throws IOException {


        handler=new FileHandler("corona_city.log",true);
        Logger.getLogger(MainPageController.class.getName()).addHandler(handler);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/page.fxml"));
        int kuceNum, punktoviNum, ambulanteNum, odrasliNum, djecaNum, stariNum;
        kuceNum = punktoviNum = ambulanteNum = odrasliNum = djecaNum = stariNum = 0;
        try {
            kuceNum = Integer.parseInt(kuce.getText());
            punktoviNum = Integer.parseInt(punktovi.getText());
            ambulanteNum = Integer.parseInt(ambulante.getText());
            odrasliNum = Integer.parseInt(odrasli.getText());
            djecaNum = Integer.parseInt(djeca.getText());
            stariNum = Integer.parseInt(stari.getText());
        } catch (NumberFormatException ex) {
            Logger.getLogger(MainPageController.class.getName()).log(Level.WARNING,ex.fillInStackTrace().toString());
            Alert alert = new Alert(Alert.AlertType.ERROR, "Enter positive integers as values.");
            alert.showAndWait();
            return;
        }
        if(odrasliNum==0 && stariNum==0){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Children can't be alone in house. You must add one or more adults or elders.");
            alert.showAndWait();
            return;
        }
        DataAboutCoronaCity dataAboutCoronaCity = new DataAboutCoronaCity(
                kuceNum, punktoviNum, ambulanteNum, odrasliNum, djecaNum, stariNum
        );
        PageController pageController = new PageController(dataAboutCoronaCity);
        loader.setController(pageController);
        Parent root = (Parent) loader.load();
        ComponentsCityDataStore.getInstance().addController(loader.getController());
        //proslijedjuje podatke o broju kuca,odraslih,djece...u drugi kontoler...
       /*pageController.setBrojKuca(kuce.getText());
       pageController.setBrojAmbulantnihVozila(ambulante.getText());
       pageController.setBrojKontrolnihPunktova(punktovi.getText());
       pageController.setBrojOdraslih(odrasli.getText());
       pageController.setBrojDjece(djeca.getText());
       pageController.setBrojStarih(stari.getText());*/


        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    public void startRotate() {
        List<RotateTransition> rotateTransitions = new ArrayList<>();
        rotateTransitions.add(new RotateTransition(Duration.millis(3000), coronaVirusRotateImageView1));
        rotateTransitions.add(new RotateTransition(Duration.millis(3000), coronaVirusRotateImageView2));
        rotateTransitions.add(new RotateTransition(Duration.millis(3000), coronaVirusRotateImageView3));
        rotateTransitions.add(new RotateTransition(Duration.millis(3000), coronaVirusRotateImageView4));
        for (RotateTransition rt : rotateTransitions) {
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
        }
    }

}
