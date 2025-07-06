package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an individual item in a purchase, including description, pre-tax cost,
 * applicable tax rate, and how the cost is split among roommates.
 */
public class Item implements Serializable {
    private String description;
    private double cost;
    private double taxRate;
    private Map<String, Double> splits; // roommate name to share ratio

    public Item(String description, double cost, double taxRate, Map<String, Double> splits) {
        this.description = description;
        this.cost = cost;
        this.taxRate = taxRate;
        this.splits = new HashMap<>(splits);
    }

    public String getDescription() {
        return description; 
    }
    
    public double getCost() {
        return cost;
    }
    
    public double getTaxRate() {
        return taxRate;
    }
    
    public Map<String, Double> getSplits() {
        return splits;
    }

    /**
     * @return total cost including tax
     */
    public double getTotalCost() {
        return cost * (1 + taxRate);
    }
}