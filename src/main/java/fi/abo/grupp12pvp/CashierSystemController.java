package fi.abo.grupp12pvp;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;

public class CashierSystemController {

    @FXML
    private ListView<String> productList;
    @FXML
    private ListView<String> cartList;
    @FXML
    private VBox cartPage;
    @FXML
    private VBox cataloguePage;
    @FXML
    private VBox checkOutPage;
    private final ShoppingCart shoppingCart = new ShoppingCart();
    private final ProductCatalogue catalogue = new ProductCatalogue();

    //Displays the shopping cart
    private void displayShoppingCart() {
        cartList.getItems().clear();

        for (Product product : shoppingCart.getProducts()) {
            cartList.getItems().add(product.displayProduct());
        }
    }

    //Displays the product catalogue
    private void displayCatalogue() {
        productList.getItems().clear();

        for (Product product : catalogue.getProducts()) {
            productList.getItems().add(product.displayProduct());
        }
    }

    //Controls which VBox page is visible
    private void showPage(VBox pageToShow) {
        cartPage.setVisible(false);
        cataloguePage.setVisible(false);
        checkOutPage.setVisible(false);

        pageToShow.setVisible(true);
    }

    @FXML
    public void initialize() {
        displayCatalogue();
        displayShoppingCart();
        showPage(cartPage);
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


