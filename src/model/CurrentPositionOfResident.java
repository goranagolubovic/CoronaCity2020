package model;

public class CurrentPositionOfResident {
    private int firstCoordinate;
    private int secondCoordinate;
    public  CurrentPositionOfResident(int i,int j){
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
