package util;

import model.PurchaseManager;
import java.io.*;

/**
 * Handles loading and saving of PurchaseManager via serialization.
 */
public class DataStore {
    private static final String FILE_NAME = "purchases.dat";

    /**
     * Saves the manager to disk.
     *
     * @param manager the PurchaseManager to save.
     * @throws IOException if an I/O error occurs.
     */
    public static void save(PurchaseManager manager) throws IOException {
        try (ObjectOutputStream out =
                 new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(manager);
        }
    }

    /**
     * Loads the manager from disk, or returns a new one if file missing/corrupt.
     *
     * @return a PurchaseManager instance.
     */
    public static PurchaseManager load() {
        try (ObjectInputStream in =
                 new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (PurchaseManager) in.readObject();
        } catch (Exception e) {
            return new PurchaseManager();
        }
    }
}