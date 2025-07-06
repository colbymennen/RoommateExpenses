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

    public Purchase(String buyer, String store, Date date, double totalCost) {
        this.buyer = buyer;
        this.store = store;
        this.date = date;
        this.totalCost = totalCost;
        this.items = new ArrayList<>();
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }
}