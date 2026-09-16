package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.payment.CardPayment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.Map;

public class CustomerController {

    @FXML
    private ListView<String> customerItemListView;

    @FXML
    private Label customerTotalLabel;

    @FXML
    private TextField cardAmountField;

    @FXML
    private Button cardPaymentButton;

    @FXML
    private Label cardPaymentStatusLabel;

    @FXML
    private Label cardPaidLabel;

    private final CardPayment cardPayment = new CardPayment();


    public void refresh(Sale sale) {
        customerItemListView.getItems().clear();

        for (Map.Entry<Product, Integer> entry : sale.getGroupedItems().entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            customerItemListView.getItems().add(
                    product.name() + " x" + quantity
            );
        }

        customerTotalLabel.setText("Total: €" + sale.getTotal().toPlainString());
    }
    private Sale sale;


    public void setSale(Sale sale) {
        this.sale = sale;
    }

    //Hanterar en kortbetalning
    private void handleCardPayment() {

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
        }
    }
    @FXML
    private void initialize() {
        cardPaymentButton.setOnAction(e -> handleCardPayment());
    }
}