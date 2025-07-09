package util;

import model.PurchaseManager;

import java.io.IOException;

/**
 * Plain wrapper that just uses the local DataStore.
 */
public class RemoteDataStore {
    /** Load from the local file. */
    public static PurchaseManager load() {
        return DataStore.load();
    }

    /** Save to the local file. */
    public static void save(PurchaseManager manager) throws IOException {
        DataStore.save(manager);
    }
}
