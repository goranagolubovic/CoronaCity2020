package model;

import java.io.Serializable;

public class Ambulance implements Serializable {
    private boolean isAmbulanceFree = true;

    public boolean getAmbulanceFree() {
        return isAmbulanceFree;
    }

    public void setAmbulanceFree(boolean ambulanceFree) {
        isAmbulanceFree = ambulanceFree;
    }
}
