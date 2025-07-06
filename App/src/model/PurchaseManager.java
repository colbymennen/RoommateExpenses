package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all purchases and the list of roommates.
 */
public class PurchaseManager implements Serializable {
    private List<Purchase> purchases;
    private List<String> roommates;

    public PurchaseManager() {
        purchases = new ArrayList<>();
        roommates = new ArrayList<>();
        // Default roommates (customize as needed)
        roommates.add("Colby");
        roommates.add("Khanh");
        roommates.add("Jehosh");
        roommates.add("Casey");
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void addPurchase(Purchase p) {
        purchases.add(p);
    }

    public void removePurchase(int index) {
        purchases.remove(index);
    }

    public List<String> getRoommates() {
        return roommates;
    }

    public void addRoommate(String name) {
        if (!roommates.contains(name)) {
            roommates.add(name);
        }
    }

    public void removeRoommate(String name) {
        roommates.remove(name);
        // Remove splits referencing removed roommate
        for (Purchase p : purchases) {
            for (Item it : p.getItems()) {
                it.getSplits().remove(name);
            }
        }
    }
}