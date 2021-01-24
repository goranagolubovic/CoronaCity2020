package util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class JavaFXUtil {
    public static void runAndWait(Runnable runnable){
        FutureTask<?> future = new FutureTask<>(runnable, null);
        Platform.runLater(future);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
