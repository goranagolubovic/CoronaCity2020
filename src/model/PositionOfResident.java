package model;

public class PositionOfResident {
    private int firstCoordinate;
    private int secondCoordinate;
    public PositionOfResident(int i, int j){
        firstCoordinate=i;
        secondCoordinate=j;
    }
    public int getFirstCoordinate(){
        return  firstCoordinate;
    }

    public int getSecondCoordinate() {
        return secondCoordinate;
    }
    public void setFirstCoordinate(int i){
        firstCoordinate=i;
    }
    public void setSecondCoordinate(int j){
        secondCoordinate=j;
    }
}
