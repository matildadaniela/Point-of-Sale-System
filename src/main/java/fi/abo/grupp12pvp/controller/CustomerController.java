package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.payment.CashPayment;
import fi.abo.grupp12pvp.service.CardReaderService;
import fi.abo.grupp12pvp.service.ReceiptExporter;
import javafx.application.Platform;
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
import java.util.concurrent.CompletableFuture;

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

    private final CardReaderService cardReaderService = new CardReaderService();

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

    @FXML
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
    @FXML
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

        // försök omvandla texten till ett belopp
        try {
            amount = new BigDecimal(input.trim());
        } catch (NumberFormatException e) {
            cardPaymentStatusLabel.setText("Invalid amount");
            return;
        }

        // kontrollera att kunden inte betalar för mycket
        if (amount.compareTo(sale.getRemainingTotal()) > 0) {
            cardPaymentStatusLabel.setText("amount is too big");
            return;
        }

        //skickar beloppet till kortläsaren och startar betalningen
        cardReaderService.waitForPayment(amount);

        // visa för kunden att systemet väntar på kortet
        cardPaymentStatusLabel.setText("waiting for card");
        //väntar på att kortläsaren ska slutföra betalningen
        waitForCardReader(amount);


    }

    private void waitForCardReader(BigDecimal amount) {
        //ser till att javafx fönstret inte fryser
        CompletableFuture.runAsync(() -> {
            try {
                // kontrollera kortläsarens status tills betalningen är färdig
                while (!cardReaderService.status().trim().equalsIgnoreCase("DONE")) {
                    Thread.sleep(500);
                }

                //Hämta resultatet från den färdiga kortbetalningen
                String result = cardReaderService.result();

                //kontrollera om betalningen blev godkänd
                boolean accepted = result.contains("<paymentState>ACCEPTED</paymentState>");

                // Nollar kortläsaren
                cardReaderService.reset();

                //uppdaterer javafx-gränssnitter på javafx tråden
                Platform.runLater(() -> {
                    if (accepted) {
                        sale.addCardPayment(amount);

                        cardPaymentStatusLabel.setText("Payment accepted");

                        cardPaidLabel.setText("Paid by card " + sale.getCardPaid().toPlainString() + "€");

                        cardAmountField.clear();

                        checkPaymentStatus();

                    } else {
                        cardPaymentStatusLabel.setText("Payment not accepted");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> cardPaymentStatusLabel.setText("Card reader error"));
            }
        });
    }



    //Hanterar en kontantbetalning
    @FXML
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
        if (cardPaymentStatusLabel != null) cardPaymentStatusLabel.setText("");
        if (cashPaymentStatusLabel != null) cashPaymentStatusLabel.setText("");
        if (cardPaidLabel != null) cardPaidLabel.setText("Paid by card: 0.00€");
        if (cashPaidLabel != null) cashPaidLabel.setText("Paid by cash: 0.00€");
        if (remainingLabel != null) remainingLabel.setText("Remaining sum to pay: €0.00");

        //Återställ växel till nästa kund
        if (changeLabel != null) changeLabel.setText("€0.00");
        if (receiptTextLabel != null) receiptTextLabel.setText("");
    }
}