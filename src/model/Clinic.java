package model;

import controller.PageController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public  class Clinic implements Serializable {
    private int capacityOfClinic;
    private int firstCoordinate;
    private int secondCoordinate;


    private  List<Resident> infectedResidents=new ArrayList<>();
    public static final Object lockerInfectedInmate=new Object();
    protected PageController.SimulationStopped simulationStopped;

    public Clinic(int capacityOfClinic, int firstCoordinate, int secondCoordinate) {
        this.capacityOfClinic = capacityOfClinic;
        this.firstCoordinate = firstCoordinate;
        this.secondCoordinate = secondCoordinate;
    }
    public List<Resident> getInfectedResidents() {
        return infectedResidents;
    }

    public void setInfectedResidents(List<Resident> infectedResidents) {
        this.infectedResidents = infectedResidents;
    }
    public boolean addInfectedResident(Resident resident){
        if(capacityOfClinic>0) {
            resident.setCurrentPositionOfResident(firstCoordinate, secondCoordinate);
            infectedResidents.add(resident);
            capacityOfClinic--;
            return true;
        }
        return false;
    }
          public List<Resident> removeRecoveredResident() {
              List<Resident> recoveredResidents = new ArrayList<>();
              if (!infectedResidents.isEmpty()) {
                  infectedResidents.stream().forEach(res -> {
                      if (!res.isInfected()) {
                          recoveredResidents.add(res);
                          infectedResidents.remove(res);
                          capacityOfClinic++;
                          System.out.println("Stanovnik " + res.getId() + "se oporavio.");
                          synchronized (lockerInfectedInmate) {
                              lockerInfectedInmate.notify();
                          }
                      }
                  });
                  return recoveredResidents;
              }
              return null;
          }

    public int getCapacityOfClinic() {
        return capacityOfClinic;
    }

    public void setCapacityOfClinic(int capacityOfClinic) {
        this.capacityOfClinic = capacityOfClinic;
    }

    public int getFirstCoordinate() {
        return firstCoordinate;
    }

    public void setFirstCoordinate(int firstCoordinate) {
        this.firstCoordinate = firstCoordinate;
    }

    public int getSecondCoordinate() {
        return secondCoordinate;
    }

    public void setSecondCoordinate(int secondCoordinate) {
        this.secondCoordinate = secondCoordinate;
    }

}
