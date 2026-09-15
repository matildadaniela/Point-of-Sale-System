package fi.abo.grupp12pvp;

import java.util.ArrayList;

public class ProductCatalogue {
    private ArrayList<Product> products;

    //Constructor
    public ProductCatalogue() {
        products = new ArrayList<>();
        loadProducts();
    }

    //Actual products in the product catalogue
    private void loadProducts() {
        products.add(new Product("12345", "Apple", 2));
        products.add(new Product("23456", "Banana", 1));
        products.add(new Product("34567", "Cinnamon stick", 3));
    }

    //Getter
    public ArrayList<Product> getProducts() {
        return products;
    }

}
