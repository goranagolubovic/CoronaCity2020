package model;


import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//import static model.City.isFieldOfMatrixFree;

public class Resident {
    private Long id;
    private String name;
    private String surname;
    private Integer yearOfBirth;
    private Gender gender;
    private Long houseID;
    private static Double bodyTemperature;
    Random temperature = new Random();
    private final double minTemperature = 34;
    private final double maxTemperature = 40;
    private static final String[] arrayOfNames = {"S", "M", "K", "V", "T", "U", "I", "L", "D", "B"};
    private static final String[] arrayOfSurnames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private final Timer t = new Timer();

    public Resident(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.yearOfBirth = yearOfBirth;
        this.gender = gender;
        this.houseID = houseID;
        Resident thisResident=this;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DecimalFormat df = new DecimalFormat("0.0");
                bodyTemperature = Double.parseDouble(df.format(minTemperature + (temperature.nextDouble() * (maxTemperature - minTemperature))));
                System.out.println(thisResident);
            }
        }, 0, 10_000);
    }

    public Resident() {
    }


    public static String getNameRandomly() {
        return arrayOfNames[new Random().nextInt(arrayOfNames.length - 1)];
    }

    public static String getSurnameRandomly() {
        return arrayOfSurnames[new Random().nextInt(arrayOfSurnames.length - 1)];
    }

    public static Gender choseGenderRandomly() {
        int gender = new Random().nextInt(2);
        if (gender == 0) {
            return Gender.Female;
        } else
            return Gender.Male;
    }

    @Override
    public String toString() {
        return "Resident{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", yearOfBirth=" + yearOfBirth +
                ", gender=" + gender +
                ", houseID=" + houseID +
                ", bodyTemperature=" + bodyTemperature +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Long getHouseID() {
        return houseID;
    }

    public void setHouseID(Long houseID) {
        this.houseID = houseID;
    }

    public static Double getBodyTemperature() {
        return bodyTemperature;
    }

    public static void setBodyTemperature(Double bodyTemperature) {
        Resident.bodyTemperature = bodyTemperature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

//}
