package model;

import components.ChildComponent;
import components.ResidentComponent;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Child extends Resident {
    public Child(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotChildException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear - yearOfBirth < 0 || currentYear - yearOfBirth > 18) {
            throw new NotChildException();
        }
    }

    @Override
    public ResidentComponent mapToComponent() {
        return new ChildComponent(this);
    }
}
