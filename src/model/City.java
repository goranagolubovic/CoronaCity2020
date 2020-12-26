package model;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Random;

public class City {
    public ArrayList<Pair> values = new ArrayList<Pair>();
    private Object[][] matrix;
    private boolean fieldOfMatrixFree=true;
    public City() {
        Random random=new Random();
        int randNumber = 15+random.nextInt(15);
        matrix=new Object[randNumber][randNumber];
        System.out.println("Test");
    }

    public Object[][] getMatrix() {
        return matrix;
    }

    public Object getFieldOfMatrix(int i,int j){
        return  matrix[i][j];
    }
    public void setFieldOfMatrix(Object o,int i,int j){
        matrix[i][j]=o;
    }
}
