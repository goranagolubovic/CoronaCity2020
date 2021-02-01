package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import model.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticController {
    @FXML
    private PieChart pieChartNumber;
    @FXML
    private PieChart pieChartType;
    @FXML
    private PieChart pieChartGender;
    public void addInPieChartNumber(){
        BufferedReader reader = null;
        String infectedNum="";
        String recoveredNum="";
        try {
            reader = new BufferedReader(new FileReader("clinic-info.txt"));
            infectedNum = reader.readLine();
            recoveredNum=reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObservableList<PieChart.Data>pieChartData= FXCollections.observableArrayList
                (new PieChart.Data("Oboljeli",Double.parseDouble(infectedNum)),
                new PieChart.Data("Oporavljeni",Double.parseDouble(recoveredNum)));
        pieChartNumber.setData(pieChartData);
        pieChartNumber.setStartAngle(60);
    }
    public  void addInPieChartType(){
        List<Resident>infectedChild=CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r->r instanceof Child).collect(Collectors.toList());
        List<Resident>infectedAdult=CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r->r instanceof Adult).collect(Collectors.toList());
        List<Resident>infectedElder=CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r->r instanceof Elder).collect(Collectors.toList());
        ObservableList<PieChart.Data>pieChartData= FXCollections.observableArrayList
                (new PieChart.Data("Odrasli",(double)infectedAdult.size()),
                        new PieChart.Data("Djeca",(double) infectedChild.size()),
                        new PieChart.Data("Stari",(double) infectedElder.size()));
        pieChartType.setData(pieChartData);
        pieChartType.setStartAngle(60);
    }

    public void addInPieChartGender() {
        List<Resident>infectedMale=CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r->r.getGender()== Gender.Male).collect(Collectors.toList());
        List<Resident>infectedFemale=CityDataStore.getInstance()
                .getInfectedResidents().stream().filter(r->r.getGender()==Gender.Female).collect(Collectors.toList());
        ObservableList<PieChart.Data>pieChartData= FXCollections.observableArrayList
                (new PieChart.Data("Muskarci",(double)infectedMale.size()),
                        new PieChart.Data("Zene",(double) infectedFemale.size()));
        pieChartGender.setData(pieChartData);
        pieChartType.setStartAngle(60);
    }
}
