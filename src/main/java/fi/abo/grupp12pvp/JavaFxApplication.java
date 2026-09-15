package fi.abo.grupp12pvp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JavaFxApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fi/abo/grupp12pvp/view/PosView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 500);
            stage.setTitle("Cash Register");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load POS view", e);
        }
    }
}