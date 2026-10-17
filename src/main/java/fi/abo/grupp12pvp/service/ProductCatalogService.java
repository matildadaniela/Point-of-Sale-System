package fi.abo.grupp12pvp.service;

import fi.abo.grupp12pvp.model.Product;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Hämtar produkter från DynamicDB via GraphQL (HTTP POST till /graphql)
@Service
public class ProductCatalogService {
    private final RestClient restClient;

    public ProductCatalogService() {
        this.restClient = RestClient.create("http://localhost:8080");
    }

    // Alla produkter i databasen
    public List<Product> getAllProducts() {
        return runProductsQuery("{ products { id product price barcode } }");
    }

    // En produkt via streckkod (tom Optional om den inte finns)
    public Optional<Product> findByBarcode(String barcode) {
        // Bara siffror tillåts, så att inget annat än en streckkod hamnar i queryn
        if (barcode == null || !barcode.matches("\\d+")) {
            return Optional.empty();
        }

        // Streckkoden skickas som TEXT (inom citattecken), så som DynamicDB förväntar sig
        List<Product> result = runProductsQuery(
                "{ products(barcode: \"" + barcode + "\") { id product price barcode } }");

        return result.stream().findFirst();
    }

    @SuppressWarnings("unchecked")
    private List<Product> runProductsQuery(String query) {
        Map<String, Object> response = restClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("query", query))
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return List.of();
        }
        if (response.get("errors") != null) {
            System.err.println("GraphQL error: " + response.get("errors"));
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null || data.get("products") == null) {
            return List.of();
        }

        List<Product> products = new ArrayList<>();
        for (Map<String, Object> row : (List<Map<String, Object>>) data.get("products")) {
            products.add(toProduct(row));
        }
        return products;
    }

    private Product toProduct(Map<String, Object> row) {
        String barcode = String.valueOf(row.get("barcode"));
        String name = String.valueOf(row.get("product"));
        BigDecimal price = new BigDecimal(String.valueOf(row.get("price")))
                .setScale(2, RoundingMode.HALF_UP);
        return new Product(barcode, name, price);
    }
}