package components;

import javafx.scene.image.Image;
import model.*;

import java.util.Calendar;

public class AdultComponent extends ResidentComponent {

    public AdultComponent(Adult adult) {
        super(adult);
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
    public Image getImageOfResidentWithClinic() {
        return new Image("view/images/clinic+adult.png");
    }

    @Override
    public Image getImageOfResidentWithHouse() {
        return new Image("view/images/home+adult.png");
    }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate,Resident resident) {
        return city.checkDistanceOfField(firstCoordinate, secondCoordinate, resident,0, Elder.class, ElderComponent.class, Adult.class, AdultComponent.class);
    }


}
