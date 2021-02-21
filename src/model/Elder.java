package model;

import components.ElderComponent;
import components.ResidentComponent;

import java.util.Calendar;


public class Elder extends Resident {
    public Elder(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotElderException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear - yearOfBirth < 65) {
            throw new NotElderException();
        }
    }

    @Override
    public ResidentComponent mapToComponent() {
        return new ElderComponent(this);
    }

}
