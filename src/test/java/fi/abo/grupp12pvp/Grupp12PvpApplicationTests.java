package fi.abo.grupp12pvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;
import fi.abo.grupp12pvp.model.CardPaymentResult;
import fi.abo.grupp12pvp.payment.CardPayment;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class Grupp12PvpApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void totalUpdatesAfterAddingItems() {

        Sale sale = new Sale();

        sale.addItem(new Product("123456", "Bread", new BigDecimal("1.50")));
        sale.addItem(new Product("234567", "Milk", new BigDecimal("2.00")));

        assertEquals(new BigDecimal("3.50"), sale.getTotal());
    }

    @Test
    void validCardPaymentShouldBeApproved() {
        CardPayment cardPayment = new CardPayment();

        CardPaymentResult result = cardPayment.pay(new BigDecimal(10.00));

        assertTrue(result.approved());
    }

}
