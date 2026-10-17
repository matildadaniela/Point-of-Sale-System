package fi.abo.grupp12pvp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import fi.abo.grupp12pvp.model.Product;
import fi.abo.grupp12pvp.model.Sale;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;



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
    void payingForAllItemsWithCreditCard() {

        Sale sale = new Sale();

        sale.addItem(new Product("123456", "Bread", new BigDecimal("1.50")));

        sale.addCardPayment(new BigDecimal("1.50"));

        assertEquals(new BigDecimal("1.50"), sale.getCardPaid());

        assertEquals(new BigDecimal("0.00"), sale.getRemainingTotal());

        assertTrue(sale.isFullyPaid());
    }

    @Test
    void payingForSomeItemsWithCreditCard() {

        Sale sale = new Sale();

        sale.addItem(new Product("123456", "Bread", new BigDecimal("1.50")));

        sale.addCardPayment(new BigDecimal("0.50"));

        assertEquals(new BigDecimal("0.50"), sale.getCardPaid());

        assertEquals(new BigDecimal("1.00"), sale.getRemainingTotal());

        assertFalse(sale.isFullyPaid());

    }


    @Test
    void payingForAllItemsWithCash() {

        Sale sale = new Sale();

        sale.addItem(new Product("123456", "Bread", new BigDecimal("1.50")));

        sale.addCashPayment(new BigDecimal("1.50"));

        assertEquals(new BigDecimal("1.50"), sale.getCashPaid());

        assertEquals(new BigDecimal("0.00"), sale.getRemainingTotal());

        assertTrue(sale.isFullyPaid());
    }

    @Test
    void payingForSomeItemsWithCash() {

        Sale sale = new Sale();

        sale.addItem(new Product("123456", "Bread", new BigDecimal("1.50")));

        sale.addCashPayment(new BigDecimal("0.50"));

        assertEquals(new BigDecimal("0.50"), sale.getCashPaid());

        assertEquals(new BigDecimal("1.00"), sale.getRemainingTotal());

        assertFalse(sale.isFullyPaid());

    }

}

