package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all purchases and defines roommates in the household.
 */
public class PurchaseManager implements Serializable {
    private List<Purchase> purchases;

    /**
     * Names of roommates. Modify to match actual users.
     */
    public static final String[] ROOMMATES = {
        "Colby", "Jehosh", "Casey", "Khanh"
    };

    /**
     * Constructs an empty PurchaseManager.
     */
    public PurchaseManager() {
        purchases = new ArrayList<>();
    }

    /**
     * Adds a purchase.
     *
     * @param p the Purchase to add.
     */
    public void addPurchase(Purchase p) {
        purchases.add(p);
    }

    /**
     * Removes the purchase at the specified index.
     *
     * @param index index of the purchase to remove.
     */
    public void removePurchase(int index) {
        purchases.remove(index);
    }

    /**
     * @return the list of all purchases.
     */
    public List<Purchase> getPurchases() {
        return purchases;
    }
}