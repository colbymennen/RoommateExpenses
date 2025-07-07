package util;

import model.Purchase;
import model.PurchaseManager;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Loads and saves PurchaseManager via a REST service.
 */
public class RemoteDataStore {
    private static final String BASE = "http://localhost:8080/api/purchases";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper M = new ObjectMapper();

    /**  
     * Fetch all purchases from the server.  
     */
    public static PurchaseManager load() {
        PurchaseManager mgr = new PurchaseManager();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();
            HttpResponse<String> resp = HTTP.send(req, BodyHandlers.ofString());

            List<Purchase> list = M.readValue(
                resp.body(),
                new TypeReference<List<Purchase>>() {}
            );
            list.forEach(mgr::addPurchase);
        } catch (Exception e) {
            e.printStackTrace();
            // fallback to empty manager
        }
        return mgr;
    }

    /**
     * Save (create or update) all purchases to the server.
     * Note: This naively POSTs every purchase; you may wish to
     * track IDs and do PUT for existing entities.
     */
    public static void save(PurchaseManager mgr) {
        mgr.getPurchases().forEach(p -> {
            try {
                String json = M.writeValueAsString(p);
                HttpRequest.Builder b = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json");

                HttpRequest req;
                if (p.getId() == null) {
                    // create
                    req = b.uri(URI.create(BASE))
                           .POST(BodyPublishers.ofString(json))
                           .build();
                } else {
                    // update
                    req = b.uri(URI.create(BASE + "/" + p.getId()))
                           .PUT(BodyPublishers.ofString(json))
                           .build();
                }
                HTTP.send(req, BodyHandlers.discarding());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
