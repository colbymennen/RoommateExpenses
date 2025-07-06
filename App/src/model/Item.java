package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an item in a purchase with description, pre-tax cost, tax rate,
 * and split ratios among roommates.
 */
public class Item implements Serializable {
    private String description;
    private double cost;
    private double taxRate;
    private Map<String, Double> splits;

    /**
     * Constructs an Item with the given parameters.
     *
     * @param description A human-readable description of the item.
     * @param cost        Pre-tax cost of the item.
     * @param taxRate     Tax rate (e.g., 0.07 for 7% tax).
     * @param splits      Map of roommate names to their share ratios.
     */
    public Item(String description, double cost, double taxRate, Map<String, Double> splits) {
        this.description = description;
        this.cost = cost;
        this.taxRate = taxRate;
        this.splits = new HashMap<>(splits);
    }

    /**
     * @return the item description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the pre-tax cost.
     */
    public double getCost() {
        return cost;
    }

    /**
     * @return the tax rate.
     */
    public double getTaxRate() {
        return taxRate;
    }

    /**
     * @return the map of roommate splits.
     */
    public Map<String, Double> getSplits() {
        return splits;
    }

    /**
     * @return total cost including tax.
     */
    public double getTotalCost() {
        return cost * (1 + taxRate);
    }
}