package fi.abo.grupp12pvp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
public class CardReaderService {
    private final RestClient restClient;

    public CardReaderService() {
        this.restClient = RestClient.create("http://localhost:9000");
    }

    public String status() {
         String status = restClient.get().uri("/cardreader/status").retrieve().body(String.class);
         return status.replace("\"", "");
    }

    public String result() {
        return restClient.get().uri("/cardreader/result").retrieve().body(String.class);
    }

    public void waitForPayment(BigDecimal amount) {
        restClient.post().uri(uriBuilder -> uriBuilder.path("/cardreader/waitForPayment").queryParam("amount", amount).build()).retrieve().toBodilessEntity();
    }

    public void reset() {
        restClient.post().uri("/cardreader/reset").retrieve().toBodilessEntity();
    }
}