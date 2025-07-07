package util;

import model.Purchase;
import model.PurchaseManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Loads and saves PurchaseManager via a shared Spring‐Boot REST service.
 * Falls back to the local DataStore if the server is unreachable.
 */
public class RemoteDataStore {
    // Point at your backend’s LAN IP:
    private static final String BASE =
        "http://192.168.1.151:8080/api/purchases";
    private static final ObjectMapper M = new ObjectMapper();

    /**
     * Fetch all purchases from the server; on failure, load locally.
     */
    public static PurchaseManager load() {
        try {
            URL url = new URL(BASE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code == 200) {
                try (InputStream in = conn.getInputStream()) {
                    List<Purchase> list = M.readValue(
                      in, new TypeReference<List<Purchase>>() {}
                    );
                    PurchaseManager mgr = new PurchaseManager();
                    list.forEach(mgr::addPurchase);
                    return mgr;
                }
            } else {
                // non-200: fall back
                return DataStore.load();
            }
        } catch (Exception e) {
            // server down or network error
            e.printStackTrace();
            return DataStore.load();
        }
    }

    /**
     * Push all purchases to the server via POST (new) or PUT (existing).
     * Errors are logged but do not interrupt the loop.
     */
    public static void save(PurchaseManager mgr) {
        mgr.getPurchases().forEach(p -> {
            try {
                String urlStr = BASE + (p.getId() != null ? "/" + p.getId() : "");
                URL url = new URL(urlStr);
                HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod(p.getId() != null ? "PUT" : "POST");
                conn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = conn.getOutputStream()) {
                    M.writeValue(os, p);
                }

                // trigger the request
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
