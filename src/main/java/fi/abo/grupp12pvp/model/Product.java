package fi.abo.grupp12pvp.model;

import java.math.BigDecimal;

public record Product(String barcode, String name, BigDecimal price) {
}