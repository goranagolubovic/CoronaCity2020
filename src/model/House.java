package model;

public class House {
    private Long id;
    public Integer numberOfInmate;  //broj ukucana
    /**
     * @param id identificator of house.
     */
    public House(Long id) {
        this.id = id;
        numberOfInmate=0;
    }
    public Long getHouseId(){
        return id;
    }
}
