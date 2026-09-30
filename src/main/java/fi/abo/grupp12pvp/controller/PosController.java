package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.service.CashBoxService;
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

public class PosController {
    private Sale sale = new Sale();
    private CustomerController customerController;
    private final CashBoxService cashBoxService = new CashBoxService();
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