package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.payment.CardPayment;
import fi.abo.grupp12pvp.payment.CashPayment;
import fi.abo.grupp12pvp.service.ReceiptExporter;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class CustomerController {

    @FXML
    private ListView<String> customerItemListView;

    @FXML
    private Label customerTotalLabel;

    //Card Payment
    @FXML
    private TextField cardAmountField;

    @FXML
    private Button cardPaymentButton;

    @FXML
    private Label cardPaymentStatusLabel;

    @FXML
    private Label cardPaidLabel;

    private final CardPayment cardPayment = new CardPayment();

    //Cash Payment
    @FXML
    private TextField cashAmountField;
    @FXML
    private Button cashPaymentButton;
    @FXML
    private Label cashPaymentStatusLabel;
    @FXML Label cashPaidLabel;
    @FXML Label changeLabel;

    private final CashPayment cashPayment = new CashPayment();

    //Remaining Sum

    @FXML private Label remainingLabel;

    //Switch från CustomerView till ApprovedView
    @FXML private VBox paymentOptionsPane;
    @FXML private VBox approvedPane;

    //Receipt

    @FXML private VBox receiptChoice;
    @FXML private VBox receiptShow;
    @FXML private Label receiptTextLabel;

    private Sale sale;

    private PosController posController;
    private final List<Product> displayedProducts = new ArrayList<>();

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public void setPosController(PosController posController) {
        this.posController = posController;
    }

    public void refresh(Sale sale) {
        customerItemListView.getItems().clear();
        displayedProducts.clear();

        for (Map.Entry<Product, Integer> entry : sale.getGroupedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            customerItemListView.getItems().add(
                    product.name() + " x" + quantity
            );
            displayedProducts.add(product);
        }

        customerTotalLabel.setText("Total: €" + sale.getTotal().toPlainString());

        if(remainingLabel != null && sale != null) {
            remainingLabel.setText("Remaining sum to pay: €" + sale.getRemainingTotal().toPlainString());
        }

        if (cashPaidLabel != null && sale != null) {
            cashPaidLabel.setText("Paid by cash: " + sale.getCashPaid().toString() + "€");
        }

        if (changeLabel != null && sale != null) {
            changeLabel.setText("Change: " + sale.getChange().toPlainString() + "€");
        }
    }

    private void handleRemoveSelected() {
        int selectedIndex = customerItemListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= displayedProducts.size()) {
            return;
        }

        Product selectedProduct = displayedProducts.get(selectedIndex);
        sale.removeItem(selectedProduct);

        refresh(sale);

        if (posController != null) {
            posController.refreshFromExternalChange();
        }
    }

    @FXML
    private Button removeButton;

    //Control that the whole sum has been paid
    private void checkPaymentStatus() {
        if (sale != null && sale.isFullyPaid()) {
            if (paymentOptionsPane != null) {
                paymentOptionsPane.setVisible(false);
                paymentOptionsPane.setManaged(false);
            }
            //Show approved window
            if (approvedPane != null) {
                approvedPane.setVisible(true);
                approvedPane.setManaged(true);
            }
        } else {
            refresh(sale);
        }
    }

    //Hanterar en kortbetalning
    private void handleCardPayment() {

        if (sale == null) return;

        //Hämtar beloppet som kunden matat in.
        String input = cardAmountField.getText();

        // Betalningen avbryts om det inte finns ett belopp
        if (input == null || input.isBlank()) {
            cardPaymentStatusLabel.setText("Enter an amount: ");
            return;
        }

        BigDecimal amount;

        //Text blir big decimal belopp + felmeddelande visas om inputen inte är ett giltigt tal
        try {
            amount = new BigDecimal(input.trim());
        } catch (NumberFormatException e) {
            cardPaymentStatusLabel.setText("Invalid amount");
            return;
        }

        //Kontrollera att kortbetalningen inte överskrider kvarvarande belopp
        BigDecimal remaining = sale.getRemainingTotal();
        if (amount.compareTo(remaining) > 0) {
            cardPaymentStatusLabel.setText("You can't pay more than remaining total (€" + remaining.toPlainString() + ")");
            return;
        }

        //Beloppet skickas till "kortbetalningstjänsten"
        CardPaymentResult result = cardPayment.pay(amount);

        //Resultatet av betalningen syns för kunden
        cardPaymentStatusLabel.setText(result.message());

        if (result.approved()) {
            //Sparar betalningen om den gick igenom
            sale.addCardPayment(amount);

            //totala beloppet uppdateras
            cardPaidLabel.setText("Paid by card: " + sale.getCardPaid().toPlainString() + "€");
            cardAmountField.clear();

            //Uppdaterar kassan med resultat och beloppet som betalades

            if (posController != null) {
                posController.updateCardTransactionResult(result, amount);
            }

            //kontrollerar att köpet är helt klart
            checkPaymentStatus();
        }
    }

    //Hanterar en kontantbetalning
    private void handleCashPayment() {
        if (sale == null) return;

        String input = cashAmountField.getText();

        if (input == null || input.isBlank()) {
            cashPaymentStatusLabel.setText("Enter an amount: ");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(input.trim());
        }
        catch (NumberFormatException e) {
            cashPaymentStatusLabel.setText("Invalid amount");
            return;
        }

        CashPaymentResult result = cashPayment.processPayment(sale, amount);

        if (posController != null) {
            posController.updateCashChange(result, amount);
        }

        cashPaymentStatusLabel.setText(result.message());

        //cashboxen öppnas om kontantbetalningen lyckades
        if (result.success()) {

            if (posController != null) {
                posController.openCashBox();
            }
            cashAmountField.clear();
            //Visa växelsumma
            if (changeLabel != null) {
                changeLabel.setText("Change: €" + result.change().toPlainString());
            }
        }

        checkPaymentStatus();
    }

    @FXML
    private void initialize() {
        cardPaymentButton.setOnAction(e -> handleCardPayment());

        if (cashPaymentButton != null) {
            cashPaymentButton.setOnAction(e -> handleCashPayment());
        }

        if (removeButton != null) {
            removeButton.setOnAction(e -> handleRemoveSelected());
        }
    }

    @FXML
    private void handleShowReceipt() {
        if (sale == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("STORE RECEIPT\n");
        for (Map.Entry<Product, Integer> entry : sale.getGroupedItems().entrySet()) {
            sb.append(entry.getKey().name())
                    .append(" x").append(entry.getValue())
                    .append(" :€").append(entry.getKey().price().multiply(new BigDecimal(entry.getValue())))
                    .append("\n");
        }

        sb.append("\n");
        sb.append("Total: €").append(sale.getTotal().toPlainString()).append("\n");
        sb.append("Paid with card: €").append(sale.getCardPaid().toPlainString()).append("\n");
        sb.append("Paid with cash: €").append(sale.getCashPaid().toPlainString()).append("\n");

        if (receiptTextLabel != null) receiptTextLabel.setText(sb.toString());
        if (receiptChoice != null) {
            receiptChoice.setVisible(false);
            receiptChoice.setManaged(false);

        }

        if (receiptShow != null) {
            receiptShow.setVisible(true);
            receiptShow.setManaged(true);
        }
    }

    @FXML
    private void handlePrintReceipt() {
        if (sale == null || sale.getItems().isEmpty()) {
            return;
        }

        Path downloadsDirectory = Path.of(
                System.getProperty("user.home"),
                "Downloads"
        );

        try {
            Files.createDirectories(downloadsDirectory);

            String fileName = "receipt-"
                    + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + ".png";
            Path receiptPath = downloadsDirectory.resolve(fileName);

            ReceiptExporter.saveAsPng(sale, receiptPath);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Receipt printed");
            alert.setHeaderText(null);
            alert.setContentText("Receipt exported to:\n" + receiptPath);
            alert.showAndWait();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Receipt error");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Could not export the receipt:\n" + exception.getMessage()
            );
            alert.showAndWait();
        }
    }


    @FXML
    private void handleApprove() {
        if(approvedPane != null) {
            approvedPane.setVisible(false);
            approvedPane.setManaged(false);
        }
        if (paymentOptionsPane != null) {
            paymentOptionsPane.setVisible(true);
            paymentOptionsPane.setManaged(true);
        }

        //Återställ kassa och historik

        if (posController != null) {
            posController.resetStatusLabels();
            posController.resetSale();
        }

        //Återställ kvitto för nästa kund

        if (receiptChoice != null) {
            receiptChoice.setVisible(true);
            receiptChoice.setManaged(true);
        }

        if(receiptShow != null) {
            receiptShow.setVisible(false);
            receiptShow.setManaged(false);
        }

        //Rensar fält för nästa kund
        if (cardAmountField != null) cardAmountField.clear();
        if (cashAmountField != null) cashAmountField.clear();
        if (cardPaymentButton != null) cardPaymentStatusLabel.setText("");
        if (cashPaymentButton != null) cashPaymentStatusLabel.setText("");

        //Återställ växel till nästa kund
        if (changeLabel != null) changeLabel.setText("€0.00");
    }
}