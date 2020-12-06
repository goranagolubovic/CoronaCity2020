package model;


public class Resident {
    private Long id;
    private String name;
    private String surname;
    private Integer yearOfBirth;
    private Gender gender;
    private Long houseID;
    private Double bodyTemperature;

    public Resident(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
        this.gender = gender;
        this.houseID = houseID;
        //TODO temperature
    }

    public Resident() {
    }

}
