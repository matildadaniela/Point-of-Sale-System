package fi.abo.grupp12pvp;

public class Product {

    private String barcode;
    private String name;
    private double price;

    // Constructor
    public Product(String barcode, String name, double price){
        this.barcode = barcode;
        this.name = name;
        this.price = price;
    }

    // Getters
    public String getBarcode() {
        return barcode;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }

    //Display product
    public void displayProduct() {
        System.out.println("Barcode: " + barcode + ", Name: " + name + ", Price: " + price + "€");
    }
}
