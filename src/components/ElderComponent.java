package components;

import javafx.scene.image.Image;
import model.*;

import java.util.Calendar;

public class ElderComponent extends ResidentComponent {
    public ElderComponent(Elder elder)  {
       super(elder);
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
    public Image getImageOfResidentWithClinic() {
        return new Image("view/images/clinic+elder.png");
    }

    @Override
    public Image getImageOfResidentWithHouse() {
        return new Image("view/images/home+elder.png");
    }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate,Resident resident) {
        return city.checkDistanceOfField(firstCoordinate, secondCoordinate,resident, 0, Elder.class, Adult.class, ElderComponent.class, AdultComponent.class);
    }


}
