package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.service.CashBoxService;
import fi.abo.grupp12pvp.service.ProductCatalogService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class PosController {
    private Sale sale = new Sale();
    private CustomerController customerController;
    private final CashBoxService cashBoxService = new CashBoxService();
    private final ProductCatalogService productCatalogService = new ProductCatalogService();
    private final List<Product> displayedProducts = new ArrayList<>();
    private final List<Sale> shelvedSales = new ArrayList<>();

    @FXML
    private TextField scanField;

    @FXML
    private Button scanButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button shelveButton;

    @FXML
    private Button resumeButton;

    @FXML
    private ListView<String> shelvedSalesListView;

    @FXML
    private ListView<String> itemListView;

    @FXML
    private Label totalLabel;

    @FXML
    private FlowPane productCataloguePane;

    @FXML
    private Label cardStatusLabel;

    @FXML
    private Label cashChangeLabel;

    @FXML
    private Label overallStatusLabel;




    public void setCustomerController(CustomerController customerController) {
        this.customerController = customerController;
    }

    @FXML
    private void initialize() {
        scanButton.setOnAction(e -> handleScan());
        removeButton.setOnAction(e -> handleRemoveSelected());
        shelveButton.setOnAction(e -> handleShelveSale());
        resumeButton.setOnAction(e -> handleResumeSale());
        createProductCatalogue();
    }

    private void handleScan() {
        String barcode = scanField.getText() == null
                ? ""
                : scanField.getText().trim();

        if (barcode.isEmpty()) {
            return;
        }

        Optional<Product> found;
        try {
            found = productCatalogService.findByBarcode(barcode);
        } catch (Exception e) {
            showError("Database error", "Could not reach the product database. Is DynamicDB running?");
            return;
        }

        if (found.isEmpty()) {
            showError("Invalid item", "Unknown barcode.");
            return;
        }

        addProductToSale(found.get());

        scanField.clear();
        scanField.requestFocus();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private void handleShelveSale() {
        if (sale.getItems().isEmpty()) {
            return; // inget att hylla om kundvagnen är tom
        }

        shelvedSales.add(sale);
        refreshShelvedList();

        // Starta en ny, tom försäljning för nästa kund
        sale = new Sale();
        refreshItemList();
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());

        if (customerController != null) {
            customerController.setSale(sale);
            customerController.refresh(sale);
        }
    }

    private void handleResumeSale() {
        int selectedIndex = shelvedSalesListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= shelvedSales.size()) {
            return;
        }

        // Om det redan finns varor i den nuvarande försäljningen, hylla den innan bytet
        if (!sale.getItems().isEmpty()) {
            shelvedSales.add(sale);
        }

        sale = shelvedSales.remove(selectedIndex);
        refreshShelvedList();

        refreshItemList();
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());

        if (customerController != null) {
            customerController.setSale(sale);
            customerController.refresh(sale);
        }
    }

    private void refreshShelvedList() {
        shelvedSalesListView.getItems().clear();
        for (int i = 0; i < shelvedSales.size(); i++) {
            Sale shelved = shelvedSales.get(i);
            shelvedSalesListView.getItems().add(
                    "Shelved sale " + (i + 1) + " - " + shelved.getItems().size()
                            + " items - €" + shelved.getTotal().toPlainString()
            );
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
        List<Product> products;
        try {
            products = productCatalogService.getAllProducts();
        } catch (Exception e) {
            System.err.println("Could not load products: " + e.getMessage());
            productCataloguePane.getChildren().add(
                    new Label("Could not load products (is DynamicDB running?)"));
            return;
        }

        for (Product product : products) {

            Button button = new Button(
                    product.name() + "\n€" + product.price()
            );
            button.setWrapText(true);
            button.setMinHeight(72);
            button.setPrefHeight(72);
            button.getStyleClass().add("product-button");
            button.setOnAction(e -> addProductToSale(product));
            productCataloguePane.getChildren().add(button);
        }
    }

    //Reset Sale och uppdatera kundvyn
    public void resetSale() {
        sale = new Sale();
        refreshItemList();

        if (totalLabel != null) {
            totalLabel.setText("Total: €" + sale.getTotal().toPlainString());
        }

        if (customerController != null) {
            customerController.setSale(sale);
            customerController.refresh(sale);
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

    //Sparar betalningshistorik

    private final List<BigDecimal> cardPaymentHistory = new ArrayList<>();
    private final List<BigDecimal> cashPaymentHistory = new ArrayList<>();

    //POS payment status
    private void checkOverallStatus() {
        if (sale != null && sale.isFullyPaid()) {
            if (overallStatusLabel != null) {
                overallStatusLabel.setText("PAYMENT COMPLETE");
                overallStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px");
            }
        }
    }

    //Uppdaterar resultatet av en korttransaktion

    public void updateCardTransactionResult(CardPaymentResult result, BigDecimal amount) {
        if (cardStatusLabel != null) {
            if (result.approved()) {
                cardPaymentHistory.add(amount);
                String historyText = cardPaymentHistory.stream()
                        .map(a -> "€" + a.toPlainString())
                                .collect(Collectors.joining(" + "));
                cardStatusLabel.setText("Card Payment: " + historyText + " paid");
                cardStatusLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");
                }
            else {
                cardStatusLabel.setText("Card Payment : DECLINED (" + result.message() + ")");
                cardStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
        checkOverallStatus();
    }

    //Uppdaterar växelsumman

    public void updateCashChange(CashPaymentResult result, BigDecimal amount) {
        if (cashChangeLabel != null) {
            if (result.success()){
                cashPaymentHistory.add(amount);

                String historyText = cashPaymentHistory.stream()
                        .map(a -> "€" + a.toPlainString())
                        .collect(Collectors.joining(" + "));

                cashChangeLabel.setText("Cash Payment: " + historyText + " received. Change to return: €" + result.change().toPlainString());
                cashChangeLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");
            }
            else {
                cashChangeLabel.setText("Not enough money received" + result.message());
                cashChangeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
        checkOverallStatus();
    }

    //Nollställ

    public void resetStatusLabels() {
        cardPaymentHistory.clear();
        cashPaymentHistory.clear();
        if (cardStatusLabel != null) cardStatusLabel.setText("");
        if (cashChangeLabel != null) cashChangeLabel.setText("");
        if (overallStatusLabel != null) overallStatusLabel.setText("");
    }

    public void openCashBox () {
        try {
            if(!cashBoxService.isOpen()) {
                cashBoxService.open();
            }
        } catch (Exception e) {
            System.err.println("Could not open cashbox: " + e.getMessage());
        }
    }


    @FXML
    public void closeCashBox() {
        try {
            if (cashBoxService.isOpen()) {
                cashBoxService.close();
            }
        } catch (Exception e) {
            System.err.println("Could not close CashBox" + e.getMessage());
        }
    }

}