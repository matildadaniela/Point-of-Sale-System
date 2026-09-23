package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.payment.CardPayment;
import fi.abo.grupp12pvp.payment.CashPayment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
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
        cashPaymentStatusLabel.setText(result.message());

        if (result.success()) {
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
    private void handleApprove() {
        if(approvedPane != null) {
            approvedPane.setVisible(false);
            approvedPane.setManaged(false);
        }
        if (paymentOptionsPane != null) {
            paymentOptionsPane.setVisible(true);
            paymentOptionsPane.setManaged(true);
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