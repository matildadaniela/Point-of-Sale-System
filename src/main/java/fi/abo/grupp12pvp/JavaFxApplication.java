package fi.abo.grupp12pvp;

import fi.abo.grupp12pvp.controller.CustomerController;
import fi.abo.grupp12pvp.controller.PosController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class JavaFxApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            loadFonts();

            // Ladda kundens vy först
            FXMLLoader customerLoader = new FXMLLoader(
                    getClass().getResource("/fi/abo/grupp12pvp/view/CustomerView.fxml"));
            Parent customerRoot = customerLoader.load();
            CustomerController customerController = customerLoader.getController();

            Stage customerStage = new Stage();
            customerStage.setTitle("Customer Display");
            customerStage.setScene(new Scene(customerRoot, 400, 700));
            customerStage.show();

            // Laddar kassörens vy
            FXMLLoader posLoader = new FXMLLoader(
                    getClass().getResource("/fi/abo/grupp12pvp/view/PosView.fxml"));
            Parent posRoot = posLoader.load();
            PosController posController = posLoader.getController();
            posController.setCustomerController(customerController);
            customerController.setSale(posController.getSale());
            customerController.setPosController(posController);

            stage.setTitle("Cash Register");
            stage.setScene(new Scene(posRoot, 1100, 700));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load views", e);
        }
    }

    private void loadFonts() {
        loadFont("/fonts/Inter-Regular.ttf");
        loadFont("/fonts/Inter-SemiBold.ttf");
        loadFont("/fonts/Inter-Bold.ttf");
        loadFont("/fonts/DMSans-Regular.ttf");
        loadFont("/fonts/DMSans-SemiBold.ttf");
        loadFont("/fonts/DMSans-Bold.ttf");
    }

    private void loadFont(String resourcePath) {
        try (InputStream fontStream = getClass().getResourceAsStream(resourcePath)) {
            if (fontStream == null || Font.loadFont(fontStream, 14) == null) {
                throw new IllegalStateException("Could not load font resource: " + resourcePath);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read font resource: " + resourcePath, exception);
        }
    }
}