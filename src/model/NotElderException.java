package model;

public class NotElderException extends Exception {
    public NotElderException() {
        System.out.println(getMessage());
    }
    @Override
    public String getMessage() {
        return "The age of the adult is not in range (65+)";
    }
}
