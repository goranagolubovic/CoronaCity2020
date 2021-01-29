package model;

import components.ComponentsCityDataStore;
import components.ResidentComponent;
import controller.PageController;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Clinic implements Serializable {
    private int capacityOfClinic;
    private int firstCoordinate;
    private int secondCoordinate;

    private static  int numberOfPatients=0;


    private List<Resident> infectedResidents = new ArrayList<>();
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

    public synchronized boolean addInfectedResident(Resident resident) {
        if (capacityOfClinic > 0) {
            resident.setCurrentPositionOfResident(firstCoordinate, secondCoordinate);
            infectedResidents.add(resident);
            capacityOfClinic--;
            numberOfPatients++;
            try {
                PrintWriter patients = new PrintWriter(new BufferedWriter(new FileWriter("clinic-info.txt")));
                patients.println(numberOfPatients);
                patients.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public synchronized List<Resident> removeRecoveredResident() {
        List<Resident> recoveredResidents = new ArrayList<>();
        for (int i = 0, infectedResidentsSize = infectedResidents.size(); i < infectedResidentsSize; i++) {
            Resident res = infectedResidents.get(i);
            if (!res.isInfected()) {
                recoveredResidents.add(res);
                infectedResidents.remove(res);
                capacityOfClinic++;
                numberOfPatients--;
                System.out.println("Stanovnik " + res.getId() + "se oporavio.");
                //zarazeni se vraca kuci nakon oporavka
                res.setCurrentPositionOfResident(res.getHouseWithConcretID(res.getHouseID()).getFirstCoordinateOfHouse(),
                        res.getHouseWithConcretID(res.getHouseID()).getSecondCoordinateOfHouse());
                try {
                    PrintWriter patients = new PrintWriter(new BufferedWriter(new FileWriter("clinic-info.txt")));
                    patients.println(numberOfPatients);
                    patients.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

}
