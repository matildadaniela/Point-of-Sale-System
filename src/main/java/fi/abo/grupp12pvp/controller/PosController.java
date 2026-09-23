package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import java.util.List;
import java.util.ArrayList;

import java.math.BigDecimal;
import java.util.Map;

public class PosController {
    private final Sale sale = new Sale();
    private CustomerController customerController;

    private final List<Product> displayedProducts = new ArrayList<>();

    @FXML
    private TextField scanField;

    @FXML
    private Button scanButton;

    @FXML
    private Button removeButton;

    @FXML
    private ListView<String> itemListView;

    @FXML
    private Label totalLabel;

    @FXML
    private FlowPane productCataloguePane;

    private final Map<String, Product> catalog = Map.of(
            "123456", new Product("123456", "Bread", new BigDecimal("1.50")),
            "234567", new Product("234567", "Milk", new BigDecimal("2.00")),
            "345678", new Product("345678", "Eggs", new BigDecimal("0.50")),
            "456789", new Product("456789", "Cheese", new BigDecimal("3.00")),
            "567890", new Product("567890", "Butter", new BigDecimal("3.20"))
    );

    public void setCustomerController(CustomerController customerController) {
        this.customerController = customerController;
    }

    @FXML
    private void initialize() {
        scanButton.setOnAction(e -> handleScan());
        removeButton.setOnAction(e -> handleRemoveSelected());
        createProductCatalogue();
    }

    private void handleScan() {
        String barcode = scanField.getText() == null
                ? ""
                : scanField.getText().trim();

        if (barcode.isEmpty()) {
            return;
        }


        Product product = catalog.get(barcode);


        if (product == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid item");
            alert.setHeaderText(null);
            alert.setContentText("Unknown barcode.");
            alert.showAndWait();
            return;
        }

        addProductToSale(product);

        scanField.clear();
        scanField.requestFocus();
    }

    private void handleRemoveSelected() {
        int selectedIndex = itemListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= displayedProducts.size()) {
            return;
        }

        Product selectedProduct = displayedProducts.get(selectedIndex);
        sale.removeItem(selectedProduct);

        refreshItemList();
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());

        if (customerController != null) {
            customerController.refresh(sale);
        }
    }

    private void refreshItemList() {
        itemListView.getItems().clear();
        displayedProducts.clear();

        for (Map.Entry<Product, Integer> entry : sale.getGroupedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            itemListView.getItems().add(
                    product.name() + " x" + quantity + " - €" + product.price() + " each"
            );
            displayedProducts.add(product);
        }
    }

    public Sale getSale() {
        return sale;
    }

    //Gör produkterna synliga för kassören
    //Anropar addProductToSale då kassören klickar på en produkt i katalogen
    private void createProductCatalogue() {
        for (Product product : catalog.values()) {

            Button button = new Button(
                    product.name() + "\n€" + product.price()
            );
            button.setPrefSize(70, 40);
            button.setOnAction(e -> addProductToSale(product));
            productCataloguePane.getChildren().add(button);
        }
    }

    public void refreshFromExternalChange() {
        refreshItemList();
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());
    }

    //Lägger till en produkt till produktkorgen både då en produkt "scannas" och då en produkt väljs från produktkatalogen
    private void addProductToSale(Product product) {
        sale.addItem(product);

        refreshItemList();
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());
        if (customerController != null) {
            customerController.refresh(sale);
        }
    }

}