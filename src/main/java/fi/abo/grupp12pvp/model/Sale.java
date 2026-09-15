package fi.abo.grupp12pvp.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Sale {
    private final List<Product> items = new ArrayList<>();

    public void addItem(Product product) {
        items.add(product);
    }

    public List<Product> getItems() {
        return List.copyOf(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(Product::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    // För customer view
    public Map<Product, Integer> getGroupedItems() {
        Map<Product, Integer> grouped = new LinkedHashMap<>();
        for (Product product : items) {
            grouped.merge(product, 1, Integer::sum);
        }
        return grouped;
    }
}