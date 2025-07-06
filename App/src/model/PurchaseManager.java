package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the list of all purchases and defines the set of roommates.
 */
public class PurchaseManager implements Serializable {
    private List<Purchase> purchases;

    // Modify these names as needed for your household
    public static final String[] ROOMMATES = {"Colby", "Khanh", "Casey", "Jehosh"};

    public PurchaseManager() {
        purchases = new ArrayList<>();
    }

    public void addPurchase(Purchase p) {
        purchases.add(p);
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }
}