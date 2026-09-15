package fi.abo.grupp12pvp.controller;

import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Map;

public class CustomerController {

    @FXML
    private ListView<String> customerItemListView;

    @FXML
    private Label customerTotalLabel;

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
}