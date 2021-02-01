package model;

import java.io.Serializable;

public class Alarm implements Serializable {
    int firstCoordinate;//pozicije zarazene osobe ,zarazena osoba i houseId
    int secondCoordinate;
    Long houseId;
    Resident resident;

    public Alarm(int firstCoordinate, int secondCoordinate, Long houseID,Resident resident) {
        this.firstCoordinate=firstCoordinate;
        this.secondCoordinate=secondCoordinate;
        this.houseId=houseID;
        this.resident=resident;
    }
    public int getFirstCoordinate(){
        return firstCoordinate;
    }
    public int getSecondCoordinate(){
        return  secondCoordinate;
    }
    public Long getHouseId(){
        return  houseId;
    }
    public Resident getResident(){
        return resident;
    }
}
