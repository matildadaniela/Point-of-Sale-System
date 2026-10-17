package fi.abo.grupp12pvp.payment;

import fi.abo.grupp12pvp.model.CardPaymentResult;
import java.math.BigDecimal;
import fi.abo.grupp12pvp.model.Sale;

//gammal kod behövs inte längre då vi har kortläsaren, ta bort
public class CardPayment {

    public CardPaymentResult pay(Sale sale, BigDecimal amount) {

        if (amount == null) {
            return new CardPaymentResult(false, "No payment amount provided.");

        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CardPaymentResult(false, "Payment must be greater than zero.");
        }

        BigDecimal remaining = sale.getRemainingTotal();
        sale.addCardPayment(amount);

        if (amount.compareTo(remaining) >= 0) {
            BigDecimal change = amount.subtract(amount);
            return new CardPaymentResult(true, "Payment complete! change: " + change);
        }

        else {
            BigDecimal newRemaining = sale.getRemainingTotal();
            return new CardPaymentResult(true, "payment partially complete, remaining sum: " + newRemaining);
        }
    }
}
