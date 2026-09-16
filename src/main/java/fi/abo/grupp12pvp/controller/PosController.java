package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.payment.CardPayment;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.Map;

public class PosController {
    private final Sale sale = new Sale();
    private CustomerController customerController;

    @FXML
    private TextField scanField;

    @FXML
    private Button scanButton;

    @FXML
    private ListView<String> itemListView;

    @FXML
    private Label totalLabel;

    @FXML
    private TextField cardAmountField;

    @FXML
    private Button cardPaymentButton;

    @FXML
    private Label cardPaymentStatusLabel;

    private final CardPayment cardPayment = new CardPayment();


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
        cardPaymentButton.setOnAction(e -> handleCardPayment());
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

        sale.addItem(product);
        itemListView.getItems().add(product.name() + " - €" + product.price());
        totalLabel.setText("Total: €" + sale.getTotal().toPlainString());

        if (customerController != null) {
            customerController.refresh(sale);
        }

        scanField.clear();
        scanField.requestFocus();
    }

    //
    private void handleCardPayment() {

        String input = cardAmountField.getText();

        if (input == null || input.isBlank()) {
            cardPaymentStatusLabel.setText("Enter an amount: ");
            return;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(input.trim());
        } catch (NumberFormatException e) {
            cardPaymentStatusLabel.setText("Invalid amount");
            return;
        }
        CardPaymentResult result = cardPayment.pay(amount);

        cardPaymentStatusLabel.setText(result.message());

        if (result.approved()) {
            cardAmountField.clear();
        }
    }
}