package components;

import model.Direction;
import model.PositionOfResident;

public class DataAboutMovement {
    private Direction direction;
    private String name;
    private Long id;
    private PositionOfResident positionOfResident;
//    public  DataAboutMovement(Direction direction,String name,Long id,PositionOfResident positionOfResident){
//        this.direction=direction;
//        this.name=name;
//        this.id=id;
//        this.positionOfResident=positionOfResident;
//    }
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PositionOfResident getPositionOfResident() {
        return positionOfResident;
    }

    public void setPositionOfResident(PositionOfResident positionOfResident) {
        this.positionOfResident = positionOfResident;
    }

}
