package fi.abo.grupp12pvp.payment;

import fi.abo.grupp12pvp.model.CashPaymentResult;
import fi.abo.grupp12pvp.model.Sale;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashPayment {

    public CashPaymentResult processPayment(Sale sale, BigDecimal cashGiven) {

        if (cashGiven == null) {
            return new CashPaymentResult(false, "No payment amount provided.", BigDecimal.ZERO);
        }

        if (cashGiven.compareTo(BigDecimal.ZERO) <= 0) {
            return new CashPaymentResult(false, "Payment amount must be greater than zero.", BigDecimal.ZERO);
        }

        BigDecimal remaining = sale.getRemainingTotal();
        sale.addCashPayment(cashGiven);

        if (cashGiven.compareTo(remaining) >= 0) {
            BigDecimal change = cashGiven.subtract(remaining);
            return new CashPaymentResult(true, "Payment complete!" + change.setScale(2, RoundingMode.HALF_UP), change);
        }

        else {
            BigDecimal newRemaining = sale.getRemainingTotal();
            return new CashPaymentResult(true, "Partial payment accepted! Remaining sum : " + newRemaining.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO);
        }
    }
}
