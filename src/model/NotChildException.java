package model;

public class NotChildException extends Exception {
    public NotChildException() {
        System.out.println(getMessage());
    }

    @Override
    public String getMessage() {
        return "The age of the child is not in range (0-18)";
    }
}
