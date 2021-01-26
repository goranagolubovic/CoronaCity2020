package components;

import javafx.scene.image.Image;
import model.*;

import java.util.Calendar;

public class ChildComponent extends ResidentComponent {
    public ChildComponent(Child child){
        super(child);
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
    public Image getImageOfResidentWithClinic() {
        return new Image("view/images/clinic+child.png");
    }

    @Override
    public Image getImageOfResidentWithHouse() {
        return new Image("view/images/home+child.png");
    }

    @Override
    public boolean checkDistance(int firstCoordinate, int secondCoordinate) {
        return true;
    }


}
