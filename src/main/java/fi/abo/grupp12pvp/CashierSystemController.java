package fi.abo.grupp12pvp;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class CashierSystemController {

    @FXML
    private VBox cartPage;

    @FXML
    private VBox cataloguePage;

    @FXML
    private VBox checkOutPage;

    //Controls which VBox page is visible
    private void showPage(VBox pageToShow) {
        cartPage.setVisible(false);
        cataloguePage.setVisible(false);
        checkOutPage.setVisible(false);

        pageToShow.setVisible(true);
    }

    @FXML
    private void showCartPage() {
        showPage(cartPage);
    }

    @FXML
    private void showCataloguePage() {
        showPage(cataloguePage);
    }

    @FXML
    private void showCheckOutPage() {
        showPage(checkOutPage);
    }
}


