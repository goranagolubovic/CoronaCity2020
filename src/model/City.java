package model;

import java.util.Random;

public class City {
    private Object[][] matrix;
    public City() {
        Random random=new Random();
        matrix=new Object[15+random.nextInt(15)][15+random.nextInt(15)];
        System.out.println(matrix.length);

    }

    public Object[][] getMatrix() {
        return matrix;
    }
}
