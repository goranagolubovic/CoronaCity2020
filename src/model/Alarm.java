package model;

public class Alarm {
    int firstCoordinate;//pozicije zarazene osobe i houseId
    int secondCoordinate;
    Long houseId;

    public Alarm(int firstCoordinate, int secondCoordinate, Long houseID) {
        this.firstCoordinate=firstCoordinate;
        this.secondCoordinate=secondCoordinate;
        this.houseId=houseID;
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

}
