package fi.abo.grupp12pvp.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Sale {
    private final List<Product> items = new ArrayList<>();

    //Kortbetalningen sammanlagt
    private BigDecimal cardPaid = BigDecimal.ZERO;

    //Kontantbetalningen sammanlagt
    private BigDecimal cashPaid = BigDecimal.ZERO;

    public BigDecimal getTotalPaid() {
        return cardPaid.add(cashPaid);
    }

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

    //Om betalningen godkänns läggs den till i systemet
    public void addCardPayment(BigDecimal amount) {
        cardPaid = cardPaid.add(amount);
    }

    //Getter
    public BigDecimal getCardPaid() {
        return cardPaid;
    }

    //Om betalningen för kontant godkänns läggs den till i systemet
    public void addCashPayment(BigDecimal amount) {
        cashPaid = cashPaid.add(amount);
    }

    //Getter
    public BigDecimal getCashPaid() {
        return cashPaid;
    }

    //Räknar ut hur mycket som ännu ska betalas
    public BigDecimal getRemainingTotal() {
        BigDecimal remaining = getTotal().subtract(getTotalPaid());
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    //Räknar ut hur mycket växel kunden ska få om hen betalat kontant
    public BigDecimal getChange() {
        BigDecimal overpaid = getTotalPaid().subtract(getTotal());
        return overpaid.compareTo(BigDecimal.ZERO) > 0 ? overpaid : BigDecimal.ZERO;
    }

    public boolean isFullyPaid() {
        return getTotalPaid().compareTo(getTotal()) >= 0 && getTotal().compareTo(BigDecimal.ZERO) > 0 ;
    }

    public boolean removeItem(Product product) {
        return items.remove(product);
    }
}