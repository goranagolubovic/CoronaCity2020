package model;

import model.Resident;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityDataStore {

    private static CityDataStore singleton;

    private List<Resident> residents;
    private List<House> houses;
    private List<ControlStation>controlStations;
    private List<Clinic>clinics;

    private CityDataStore() {
        residents = new ArrayList<>();
        houses = new ArrayList<>();
        controlStations=new ArrayList<>();
        clinics=new ArrayList<>();
    }

    public static CityDataStore getInstance() {
        return (singleton == null) ? (singleton = new CityDataStore()) : singleton;
    }

    public synchronized void addResident(Resident resident) {
        resident.setId(createResidentID());
        residents.add(resident);
    }

    public synchronized void addHouse(House house) {
        house.setId(createHouseID());
        houses.add(house);
    }
    public synchronized void addControlStation(ControlStation controlStation){
        controlStations.add(controlStation);
    }
    public synchronized void addClinic(Clinic clinic){
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

    public  List<ControlStation> getControlStations(){
        return controlStations;
    }
    public void setControlStations(List<ControlStation>controlStations){
        this.controlStations=controlStations;
    }
}
