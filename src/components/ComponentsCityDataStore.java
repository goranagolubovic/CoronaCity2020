package components;

import controller.PageController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ComponentsCityDataStore implements Serializable {

    private static ComponentsCityDataStore singleton;

    private PageController pageController;

    private List<ResidentComponent> residentComponents;

    private ComponentsCityDataStore() {
        residentComponents = new ArrayList<>();
    }

    public static ComponentsCityDataStore getInstance() {
        return (singleton == null) ? (singleton = new ComponentsCityDataStore()) : singleton;
    }

    public PageController getPageController() {
        return pageController;
    }

    public void setPageController(PageController pageController) {
        this.pageController = pageController;
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

    public void addController(PageController controller) {
        this.pageController = controller;
    }

}

