package util;

import model.PurchaseManager;
import java.io.*;

/**
 * Handles loading and saving of PurchaseManager via serialization.
 */
public class DataStore {
    private static final String FILE_NAME = "purchases.dat";

    public static void save(PurchaseManager manager) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(FILE_NAME))) {
            out.writeObject(manager);
        }
    }

    public static PurchaseManager load() {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(FILE_NAME))) {
            return (PurchaseManager) in.readObject();
        } catch (Exception e) {
            return new PurchaseManager();
        }
    }
}