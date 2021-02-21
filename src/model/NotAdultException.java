package model;

public class NotAdultException extends Exception {
    public NotAdultException() {
        System.out.println(getMessage());
    }

    @Override
    public String getMessage() {
        return "The age of the adult is not in range (18-65)";
    }
}
