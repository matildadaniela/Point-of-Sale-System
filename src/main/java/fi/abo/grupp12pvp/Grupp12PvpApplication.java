package fi.abo.grupp12pvp;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Grupp12PvpApplication {

    public static void main(String[] args) {
        SpringApplication.run(Grupp12PvpApplication.class, args);
        Application.launch(JavaFxApplication.class, args);
    }
}