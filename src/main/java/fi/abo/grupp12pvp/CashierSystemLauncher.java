package fi.abo.grupp12pvp;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CashierSystemLauncher {
    public static void main(String[] args) {
        Application.launch(CashierSystemApplication.class, args);
    }
}