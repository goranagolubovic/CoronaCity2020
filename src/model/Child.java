package model;

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
    public boolean checkBounds(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        switch (direction) {
            case Up -> {
                return secondCoordinate > 0;
            }
            case Left -> {
                return firstCoordinate > 0;
            }
            case Right -> {
                return firstCoordinate < city.getMatrix().length - 1;
            }
            case Bottom -> {
                return secondCoordinate < city.getMatrix().length - 1;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public Image getImageOfResident() { return new Image("view/images/child.png"); }

    @Override
    public Image getImageOfResidentWithThermometer() { return new Image("view/images/thermometer+child.png"); }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate) {
        return true;
    }


}
