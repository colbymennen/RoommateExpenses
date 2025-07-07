package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a roommate Purchase made up of Items.
 * Must have a no-arg constructor for JSON deserialization.
 */
public class Purchase implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String buyer;
    private String store;
    private Date date;
    private double totalCost;
    private List<Item> items = new ArrayList<>();

    /** Jackson needs this. */
    public Purchase() { }

    public Purchase(String buyer, String store, Date date, double totalCost) {
        this.buyer = buyer;
        this.store = store;
        this.date = date;
        this.totalCost = totalCost;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBuyer() { return buyer; }
    public void setBuyer(String buyer) { this.buyer = buyer; }

    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public void addItem(Item item) { items.add(item); }
    public void removeItem(Item item) { items.remove(item); }
}
