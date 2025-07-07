package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates a purchase.
 */
public class Purchase implements Serializable {
    private Long id;                // <— new

    private String buyer;
    private String store;
    private Date date;
    private double totalCost;
    private List<Item> items = new ArrayList<>();

    public Purchase() { }

    public Purchase(String buyer, String store, Date date, double totalCost) {
        this.buyer     = buyer;
        this.store     = store;
        this.date      = date;
        this.totalCost = totalCost;
    }

    // ——— new id getter/setter ———
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getBuyer()        { return buyer; }
    public void setBuyer(String b)  { this.buyer = b; }

    public String getStore()        { return store; }
    public void setStore(String s)  { this.store = s; }

    public Date getDate()           { return date; }
    public void setDate(Date d)     { this.date = d; }

    public double getTotalCost()            { return totalCost; }
    public void setTotalCost(double total)  { this.totalCost = total; }

    public List<Item> getItems()            { return items; }
    public void setItems(List<Item> items)  { this.items = items; }

    public void addItem(Item item)          { items.add(item); }
    public void removeItem(Item item)       { items.remove(item); }
}
