package fi.abo.grupp12pvp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class CashierSystemApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    //Start spring boot
    @Override
    public void init() {
        applicationContext = SpringApplication.run(
                CashierSystemLauncher.class,
                getParameters().getRaw().toArray(new String[0])
        );
    }

    //Open javafx
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(CashierSystemApplication.class.getResource("/CashierSystem-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Cashier System");
        stage.setScene(scene);
        stage.show();
    }

    //Quits spring boot and javafx
    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}