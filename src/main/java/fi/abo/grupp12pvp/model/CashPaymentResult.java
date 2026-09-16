package fi.abo.grupp12pvp.model;

import java.math.BigDecimal;

//Resultat av en kontantbetalning
public record CashPaymentResult(boolean success, String message, BigDecimal change) {

}
