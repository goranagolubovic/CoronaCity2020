package model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Elder extends Resident {
    public Elder(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotElderException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear - yearOfBirth < 65) {
            throw new NotElderException();
        }
    }

    @Override
    public boolean checkBounds(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate > house.getSecondCoordinateOfHouse() - 3);
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate > house.getFirstCoordinateOfHouse() - 3);
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate < house.getFirstCoordinateOfHouse() + 3);
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate < house.getSecondCoordinateOfHouse() + 3);
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public Image getImageOfResident() {
        return new Image("view/images/elder.png");
    }

    @Override
    public Image getImageOfResidentWithThermometer() { return new Image("view/images/thermometer+elder.png"); }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate) {
        return city.checkDistanceOfField(firstCoordinate, secondCoordinate, 1, Elder.class, Adult.class);
    }


}
