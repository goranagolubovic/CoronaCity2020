package model;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Adult extends Resident {

    public Adult(Long id, String name, String surname, Integer yearOfBirth, Gender gender, Long houseID) throws NotAdultException {
        super(id, name, surname, yearOfBirth, gender, houseID);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (currentYear - yearOfBirth < 18 || currentYear - yearOfBirth > 65) {
            throw new NotAdultException();
        }
    }

    @Override
    public boolean checkBounds(House house, Direction direction, Integer firstCoordinate, Integer secondCoordinate, City city) {
        switch (direction) {
            case Up -> {
                return (secondCoordinate > 0 && secondCoordinate > house.getSecondCoordinateOfHouse() - Math.round(0.25 * city.getMatrix().length));
            }
            case Left -> {
                return (firstCoordinate > 0 && firstCoordinate > house.getFirstCoordinateOfHouse() - Math.round(0.25 * city.getMatrix().length));
            }
            case Right -> {
                return (firstCoordinate < city.getMatrix().length - 1 && firstCoordinate < house.getFirstCoordinateOfHouse() + Math.round(0.25 * city.getMatrix().length));
            }
            case Bottom -> {
                return (secondCoordinate < city.getMatrix().length - 1 && secondCoordinate < house.getSecondCoordinateOfHouse() + Math.round(0.25 * city.getMatrix().length));
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public Image getImageOfResident() {
        return new Image("view/images/adult.png");
    }

    @Override
    public Image getImageOfResidentWithThermometer() { return new Image("view/images/thermometer+adult.png"); }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate) {
        return city.checkDistanceOfField(firstCoordinate, secondCoordinate, 1, Elder.class, Adult.class);
    }


}
