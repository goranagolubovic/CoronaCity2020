package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    public Object locker = new Object();
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

    public MainPageController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void startSimulation(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/page.fxml"));
        // TODO: Provjeriti da li su u pitanju sve brojevi (to gledaju oni dosta)
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
}
