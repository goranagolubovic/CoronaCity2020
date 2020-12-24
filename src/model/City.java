package model;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Random;

public class City {
    public ArrayList<Pair> values = new ArrayList<Pair>();
    private static Object[][] matrix;
    private boolean fieldOfMatrixFree=true;
    public City() {
        Random random=new Random();
        matrix=new Object[15+random.nextInt(15)][15+random.nextInt(15)];

    }

    public static Object[][] getMatrix() {
        return matrix;
    }

    public Object getFieldOfMatrix(int i,int j){
        return  matrix[i][j];
    }
    public void  setFieldOfMatrix(Object o,int i,int j){
        matrix[i][j]=o;
    }
}
