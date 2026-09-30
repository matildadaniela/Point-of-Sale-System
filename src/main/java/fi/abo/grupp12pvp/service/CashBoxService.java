package fi.abo.grupp12pvp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CashBoxService {
    private final RestClient restClient;

    public CashBoxService() {
        this.restClient = RestClient.create("http://localhost:8000");
    }

    public void open() {
        restClient.post().uri("/cashbox/open").retrieve().toBodilessEntity();
    }

    public void close() {
        restClient.post().uri("/cashbox/close").retrieve().toBodilessEntity();
    }

    public boolean isOpen() {
        Boolean status = restClient.get().uri("/cashbox/status").retrieve().body(Boolean.class);

        return Boolean.TRUE.equals(status);
    }

}
