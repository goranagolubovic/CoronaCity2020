package model;

public class House {
    private Long id;
    private int firstCoordinateOfHouse;
    private  int secondCoordinateOfHouse;
    public Integer numberOfInmate;  //broj ukucana
    /**
     * @param id identificator of house.
     */
    public House(Long id) {
        this.id = id;
        numberOfInmate=0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfInmate() {
        return numberOfInmate;
    }

    public void setNumberOfInmate(Integer numberOfInmate) {
        this.numberOfInmate = numberOfInmate;
    }
    public int getFirstCoordinateOfHouse(){
        return firstCoordinateOfHouse;
    }
    public  int getSecondCoordinateOfHouse(){
        return secondCoordinateOfHouse;
    }
    public void setFirstCoordinateOfHouse(int i){
        firstCoordinateOfHouse=i;
    }
    public void setSecondCoordinateOfHouse(int j){
        secondCoordinateOfHouse=j;
    }
}
