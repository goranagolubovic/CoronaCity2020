package model;

import java.time.Year;
import java.util.Calendar;

public class Child extends Resident {
    public Child(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotChildException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear= Calendar.getInstance().get(Calendar.YEAR);
        if(currentYear-yearOfBirth<0 || currentYear-yearOfBirth>18){
            throw new NotChildException();
        }
    }
}
