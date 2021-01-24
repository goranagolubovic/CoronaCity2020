package controller;

import model.House;

public class DataAboutCoronaCity {
    private int brojKuca;
    private int kontrolniPunktovi;
    private int ambulantnaVozila;
    private int odrasli;
    private int djeca;
    private int stari;

    private House[] arrayOfHouses = new House[100];

    public DataAboutCoronaCity(int brojKuca, int kontrolniPunktovi, int ambulantnaVozila, int odrasli, int djeca, int stari) {
        this.brojKuca = brojKuca;
        this.kontrolniPunktovi = kontrolniPunktovi;
        this.ambulantnaVozila = ambulantnaVozila;
        this.odrasli = odrasli;
        this.djeca = djeca;
        this.stari = stari;
    }

    public void setBrojKuca(String text) {
        brojKuca = Integer.parseInt(text);
    }

    public void setBrojAmbulantnihVozila(String text) {
        ambulantnaVozila = Integer.parseInt(text);
    }

    public void setBrojKontrolnihPunktova(String text) {
        kontrolniPunktovi = Integer.parseInt(text);
    }

    public void setBrojOdraslih(String text) {
        odrasli = Integer.parseInt(text);
    }

    public void setBrojDjece(String text) {
        djeca = Integer.parseInt(text);
    }

    public void setBrojStarih(String text) {
        stari = Integer.parseInt(text);
    }

    public int getBrojKuca() {
        return brojKuca;
    }

    public int getAmbulantnaVozila() {
        return ambulantnaVozila;
    }

    public int getKontrolniPunktovi() {
        return kontrolniPunktovi;
    }

    public int getOdrasli() {
        return odrasli;
    }

    public int getDjeca() {
        return djeca;
    }

    public int getStari() {
        return stari;
    }
    public void setArrayOfHouses(int i,House house){
        arrayOfHouses[i]=house;
    }
    public Long getHouseIDFromArray(int i){
        return arrayOfHouses[i].getId();
    }
    public House [] getArrayOfHouses(){
        return arrayOfHouses;
    }
}
