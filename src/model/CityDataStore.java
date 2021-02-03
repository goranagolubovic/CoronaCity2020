package model;

import components.ResidentComponent;
import controller.PageController;
import model.Resident;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityDataStore implements Serializable {

    private static CityDataStore singleton;

    private List<Resident> residents;
    private List<House> houses;
    private List<ControlStation> controlStations;
    private List<Clinic> clinics;
    private int citySize;
    private List<Resident>infectedResidents;
    private List<Resident> recoveredResidents;
    private PageController pageController;
    private Long startTimeOfSimulation;
    private Long endTimeOfSimulation;

    public Long getEndTimeOfSimulation() {
        return endTimeOfSimulation;
    }

    public void setEndTimeOfSimulation(Long endTimeOfSimulation) {
        this.endTimeOfSimulation = endTimeOfSimulation;
    }

    public Long getStartTimeOfSimulation() {
        return startTimeOfSimulation;
    }

    public void setStartTimeOfSimulation(Long startTimeOfSimulation) {
        this.startTimeOfSimulation = startTimeOfSimulation;
    }


    public PageController getPageController() {
        return pageController;
    }

    public void setPageController(PageController pageController) {
        this.pageController = pageController;
    }

    public List<Resident> getInfectedResidents() {
        return infectedResidents;
    }

    public void setInfectedResidents(List<Resident> infectedResidents) {
        this.infectedResidents = infectedResidents;
    }
    public  void setRecoveredResidents(List<Resident>recoveredResidents){this .recoveredResidents=recoveredResidents;}
    private CityDataStore() {
        residents = new ArrayList<>();
        houses = new ArrayList<>();
        controlStations = new ArrayList<>();
        clinics = new ArrayList<>();
        infectedResidents=new ArrayList<>();
        recoveredResidents=new ArrayList<>();
    }

    public static CityDataStore getInstance() {
        return (singleton == null) ? (singleton = new CityDataStore()) : singleton;
    }
    public  synchronized  void addInfectedResident(Resident resident){
        infectedResidents.add(resident);
    }
    public  synchronized void addRecoveredResident(Resident resident){recoveredResidents.add(resident);}
    public synchronized void addResident(Resident resident) {
        resident.setId(createResidentID());
        residents.add(resident);
    }

    public synchronized void addHouse(House house) {
        house.setId(createHouseID());
        houses.add(house);
    }

    public synchronized void addControlStation(ControlStation controlStation) {
        controlStations.add(controlStation);
    }

    public synchronized void addClinic(Clinic clinic) {
        clinics.add(clinic);
    }


    private Long createResidentID() {
        Optional<Resident> optionalResident = residents.stream().max((a, b) -> (int) (a.getId() - b.getId()));
        //ako postoji maksimum
        if (optionalResident.isPresent())
            return optionalResident.get().getId() + 1;
        else
            return 1L;
    }

    private Long createHouseID() {
        Optional<House> optionalHouse = houses.stream().max((a, b) -> (int) (a.getId() - b.getId()));
        //ako postoji maksimum
        if (optionalHouse.isPresent())
            return optionalHouse.get().getId() + 1;
        else
            return 1L;
    }

    public List<Resident> getResidents() {
        return residents;
    }

    public void setResidents(List<Resident> residents) {
        this.residents = residents;
    }

    public List<House> getHouses() {
        return houses;
    }

    public void setHouses(List<House> houses) {
        this.houses = houses;
    }

    public List<ControlStation> getControlStations() {
        return controlStations;
    }

    public void setControlStations(List<ControlStation> controlStations) {
        this.controlStations = controlStations;
    }

    public void setCitySize(int citySize) {
        this.citySize = citySize;
    }

    public int getCitySize() {
        return citySize;
    }

    public List<Clinic> getClinics() {
        return clinics;
    }

    public void setClinics(List<Clinic> clinics) {
        this.clinics = clinics;
    }

    public void loadData(CityDataStore deserializabledData) {
        singleton = deserializabledData;
    }

    public List<Resident> getRecoveredResidents() {
        return recoveredResidents;
    }

    public void addController(PageController controller) {
        this.pageController=controller;
    }
}
