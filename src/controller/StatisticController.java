package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import model.*;

import javafx.scene.input.MouseEvent;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StatisticController {
    Scene previousScene;
    int infectedPatients;
    int recoveredPatients;
    int infectedChild;
    int infectedAdult;
    int infectedElder;
    int infectedMale;
    int infectedFemale;

    @FXML
    private PieChart pieChartNumber;
    @FXML
    private PieChart pieChartType;
    @FXML
    private PieChart pieChartGender;


    public StatisticController() {

    }

    public StatisticController(Scene scene) {
        this.previousScene = scene;
        infectedChild = (int) (CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r -> r instanceof Child).collect(Collectors.toList()).size());
        infectedAdult = infectedAdult = (int) (CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r -> r instanceof Adult).collect(Collectors.toList()).size());
        infectedElder = (int) (CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r -> r instanceof Elder).collect(Collectors.toList()).size());
        infectedMale = (int) (CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r -> r.getGender() == Gender.Male).collect(Collectors.toList()).size());
        infectedFemale = (int) (CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r -> r.getGender() == Gender.Female).collect(Collectors.toList()).size());
        infectedPatients = (int) CityDataStore.getInstance().getInfectedResidents().size();
        recoveredPatients = (int) CityDataStore.getInstance().getRecoveredResidents().size();
    }

    public void addInPieChartType() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList
                (new PieChart.Data("Odrasli", (double) infectedAdult),
                        new PieChart.Data("Djeca", (double) infectedChild),
                        new PieChart.Data("Stari", (double) infectedElder));
        pieChartType.setData(pieChartData);
        pieChartType.setStartAngle(60);
    }

    public void addInPieChartGender() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList
                (new PieChart.Data("Muskarci", (double) infectedMale),
                        new PieChart.Data("Zene", (double) infectedFemale));
        pieChartGender.setData(pieChartData);
        pieChartGender.setStartAngle(60);
    }

    public void addInPieChartNumber() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList
                (new PieChart.Data("Zarazeni", (double) infectedPatients),
                        new PieChart.Data("Oporavljeni", (double) recoveredPatients));
        pieChartNumber.setData(pieChartData);
        pieChartNumber.setStartAngle(60);
    }

    @FXML
    void returnToSimulation(MouseEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(previousScene);
        stage.show();
    }

    @FXML
    void downloadReport(MouseEvent e) {
        try {
            DirectoryChooser dc = new DirectoryChooser();
            File f = dc.showDialog(pieChartGender.getScene().getWindow());
            String filePath = f.getAbsolutePath();
            PrintWriter printWriter = new PrintWriter(new File(filePath + "/report.csv"));
            StringBuilder sb = new StringBuilder();
            sb.append("Zarazeno ");
            sb.append(infectedPatients);
            sb.append(",");
            sb.append("Oporavljeno ");
            sb.append(recoveredPatients);
            sb.append("\r\n");

            sb.append("Zarazeno odraslih ");
            sb.append(infectedAdult);
            sb.append(",");
            sb.append("Zarazeno djece ");
            sb.append(infectedChild);
            sb.append(",");
            sb.append("Zarazeno starih ");
            sb.append(infectedElder);
            sb.append("\r\n");

            sb.append("Zarazeno muskih ");
            sb.append(infectedMale);
            sb.append(",");
            sb.append("Zarazeno zenskih ");
            sb.append(infectedFemale);
            sb.append("\r\n");

            printWriter.print(sb.toString());
            printWriter.close();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Izvjestaj je preuzet.");
                alert.showAndWait();
            });
        } catch (FileNotFoundException fileNotFoundException) {
            Logger.getLogger(PageController.class.getName()).log(Level.WARNING, fileNotFoundException.fillInStackTrace().toString());
        }

    }
}
