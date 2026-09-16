package fi.abo.grupp12pvp.payment;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import java.math.BigDecimal;

//Hanterar en "kortbetalning" som godkänns om summan är större än 0.
public class CardPayment {

    public CardPaymentResult pay(BigDecimal amount) {

        if (amount == null) {
            return new CardPaymentResult(false, "No payment amount provided.");

        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CardPaymentResult(false, "Payment must be greater than zero.");
        }
        return new CardPaymentResult(true, "Payment approved.");
    }
}
