package fi.abo.grupp12pvp;

import java.util.ArrayList;

public class ShoppingCart {

    private ArrayList<Product> products;

    //Constructor
    public ShoppingCart () {
        products = new ArrayList<>();
    }

    //Add product to cart
    public void addProduct(Product product) {
        products.add(product);
    }

    //Getter
    public ArrayList<Product> getProducts() {
        return products;
    }

}
