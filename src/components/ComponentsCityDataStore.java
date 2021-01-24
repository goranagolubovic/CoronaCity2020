package components;

import components.ResidentComponent;
import model.Clinic;
import model.ControlStation;
import model.House;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentsCityDataStore implements Serializable {

    private static ComponentsCityDataStore singleton;

    private List<ResidentComponent> residentComponents;

    private ComponentsCityDataStore() {
        residentComponents = new ArrayList<>();
    }

    public static ComponentsCityDataStore getInstance() {
        return (singleton == null) ? (singleton = new ComponentsCityDataStore()) : singleton;
    }

    public synchronized void addResident(ResidentComponent resident) {
        residentComponents.add(resident);
    }


    public List<ResidentComponent> getResidents() {
        return residentComponents;
    }

    public void setResidents(List<ResidentComponent> residentComponents) {
        this.residentComponents = residentComponents;
    }

}

