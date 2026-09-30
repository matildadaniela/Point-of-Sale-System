package fi.abo.grupp12pvp;

import fi.abo.grupp12pvp.controller.CustomerController;
import fi.abo.grupp12pvp.controller.PosController;
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
            // Ladda kundens vy först
            FXMLLoader customerLoader = new FXMLLoader(
                    getClass().getResource("/fi/abo/grupp12pvp/view/CustomerView.fxml"));
            Parent customerRoot = customerLoader.load();
            CustomerController customerController = customerLoader.getController();

            Stage customerStage = new Stage();
            customerStage.setTitle("Customer Display");
            customerStage.setScene(new Scene(customerRoot, 400, 500));
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
            stage.setScene(new Scene(posRoot, 500, 700));
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load views", e);
        }
    }
}