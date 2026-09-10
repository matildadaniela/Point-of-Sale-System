package fi.abo.grupp12pvp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = SpringApplication.run(
                Grupp12PvpApplication.class,
                getParameters().getRaw().toArray(new String[0])
        );
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label("Grupp 12 PVP");

        StackPane root = new StackPane(label);

        Scene scene = new Scene(root, 600, 400);

        stage.setTitle("Grupp 12 PVP");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}