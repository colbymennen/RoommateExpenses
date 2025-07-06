package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates a purchase, storing buyer, store, date, total cost, and items.
 */
public class Purchase implements Serializable {
    private String buyer;
    private String store;
    private Date date;
    private double totalCost;
    private List<Item> items;

    /**
     * Constructs a Purchase with the specified parameters.
     *
     * @param buyer     The name of the buyer.
     * @param store     The store where the purchase was made.
     * @param date      The date of purchase.
     * @param totalCost The total cost of the purchase.
     */
    public Purchase(String buyer, String store, Date date, double totalCost) {
        this.buyer = buyer;
        this.store = store;
        this.date = date;
        this.totalCost = totalCost;
        this.items = new ArrayList<>();
    }

    /**
     * @return the buyer's name.
     */
    public String getBuyer() {
        return buyer;
    }

    /**
     * @param buyer the new buyer's name.
     */
    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    /**
     * @return the store name.
     */
    public String getStore() {
        return store;
    }

    /**
     * @param store the new store name.
     */
    public void setStore(String store) {
        this.store = store;
    }

    /**
     * @return the purchase date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the new purchase date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the total cost.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * @param totalCost the new total cost.
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * @return the list of items in this purchase.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Adds an item to this purchase.
     *
     * @param item the Item to add.
     */
    public void addItem(Item item) {
        items.add(item);
    }
}