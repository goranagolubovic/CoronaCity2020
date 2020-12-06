package model;

import java.util.Calendar;

public class Adult extends Resident {

    public Adult(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotAdultException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear= Calendar.getInstance().get(Calendar.YEAR);
        if(currentYear-yearOfBirth<18 || currentYear-yearOfBirth>65){
            throw new NotAdultException();
        }
    }
}
