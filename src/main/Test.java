package main;

import model.City;
import model.House;

public class Test {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new City();
            new House(4L);
        }
    }
}
