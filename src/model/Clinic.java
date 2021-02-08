package model;

import components.ComponentsCityDataStore;
import components.ResidentComponent;
import controller.MainPageController;
import controller.PageController;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Clinic implements Serializable {
    private int capacityOfClinic;
    private int firstCoordinate;
    private int secondCoordinate;

    public static AtomicInteger numberOfInfected = new AtomicInteger(0);
    public static AtomicInteger numberOfRecovered=new AtomicInteger(0);


    private List<Resident> infectedResidents = new ArrayList<>();
    protected PageController.SimulationStopped simulationStopped;
    private int clinicID;

    public Clinic(int clinicID,int capacityOfClinic, int firstCoordinate, int secondCoordinate) {
        this.capacityOfClinic = capacityOfClinic;
        this.firstCoordinate = firstCoordinate;
        this.secondCoordinate = secondCoordinate;
        this.clinicID=clinicID;
    }
    private  static final Object numberOfInfectedInClinics=new Object();

    public List<Resident> getInfectedResidents() {
        return infectedResidents;
    }

    public void setInfectedResidents(List<Resident> infectedResidents) {
        this.infectedResidents = infectedResidents;
    }

    public synchronized boolean addInfectedResident(Resident resident) {
        Logger.getLogger(Clinic.class.getName()).addHandler(MainPageController.handler);
        if (capacityOfClinic > 0) {
            resident.setCurrentPositionOfResident(firstCoordinate, secondCoordinate);
            infectedResidents.add(resident);
            CityDataStore.getInstance().addInfectedResident(resident);
            capacityOfClinic--;
           synchronized (numberOfInfectedInClinics) {
                numberOfInfected.getAndIncrement();
                try {
                    PrintWriter patients = new PrintWriter(new BufferedWriter(new FileWriter("clinic-info.txt")));
                    patients.println(numberOfInfected);
                    patients.close();
                    return true;
                } catch (IOException e) {
                    Logger.getLogger(PageController.class.getName()).log(Level.WARNING,e.fillInStackTrace().toString());
                }
            }
        }
        return false;
    }

    public synchronized List<Resident> removeRecoveredResident() {
        Logger.getLogger(Clinic.class.getName()).addHandler(MainPageController.handler);
        List<Resident> recoveredResidents = new ArrayList<>();
        for (int i = 0 ; i < infectedResidents.size(); i++) {
            try {
                Resident res = infectedResidents.
                        get(i);
                if (!res.isInfected()) {
                    recoveredResidents.add(res);
                    CityDataStore.getInstance().addRecoveredResident(res);
                    capacityOfClinic++;
                    infectedResidents.remove(res);
                    synchronized (numberOfInfectedInClinics) {
                        numberOfRecovered.getAndIncrement();
                        try {
                            PrintWriter patients = new PrintWriter(new BufferedWriter(new FileWriter("clinic-info.txt")));
                            patients.println(numberOfInfected);
                            patients.println(numberOfRecovered);
                            patients.close();
                        } catch (IOException e) {
                            Logger.getLogger(PageController.class.getName()).log(Level.WARNING, e.fillInStackTrace().toString());
                        }
                    }
                    System.out.println("Stanovnik " + res.getId() + "se oporavio.");
                    //zarazeni se vraca kuci nakon oporavka
                    res.setCurrentPositionOfResident(res.getHouseWithConcretID(res.getHouseID()).getFirstCoordinateOfHouse(),
                            res.getHouseWithConcretID(res.getHouseID()).getSecondCoordinateOfHouse());
                    Optional<ResidentComponent> opt = ComponentsCityDataStore
                            .getInstance()
                            .getResidents()
                            .stream()
                            .filter(r -> res.getId() == r.getResident().getId())
                            .findFirst();
                    if (opt.isPresent()) {
                        synchronized (opt.get().getLockerInfected()) {
                            opt.get().getLockerInfected().notifyAll();
                        }
                    }
                }
            }
            catch (IndexOutOfBoundsException e){
                Logger.getLogger(Clinic.class.getName()).log(Level.WARNING,e.fillInStackTrace().toString());
            }
        }
        return recoveredResidents;
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

    public int getID() {
        return clinicID;
    }
}
