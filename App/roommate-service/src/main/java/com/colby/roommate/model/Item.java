package com.colby.roommate.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity representing a line‐item in a purchase.
 */
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private double cost;
    private double taxRate;

    @ElementCollection
    @CollectionTable(
        name = "item_splits",
        joinColumns = @JoinColumn(name = "item_id")
    )
    @MapKeyColumn(name = "roommate")
    @Column(name = "ratio")
    private Map<String, Double> splits = new HashMap<>();

    public Item() { }

    public Item(String description, double cost, double taxRate, Map<String, Double> splits) {
        this.description = description;
        this.cost = cost;
        this.taxRate = taxRate;
        this.splits = new HashMap<>(splits);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public Map<String, Double> getSplits() { return splits; }
    public void setSplits(Map<String, Double> splits) { this.splits = splits; }

    /** @return total cost including tax */
    public double getTotalCost() {
        return cost * (1 + taxRate);
    }
}
