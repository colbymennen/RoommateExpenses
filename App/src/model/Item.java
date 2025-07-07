package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single line‐item in a Purchase.
 * Jackson will ignore any JSON fields we don’t explicitly model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String description;
    private double cost;
    private double taxRate;
    private Map<String, Double> splits = new HashMap<>();

    /** Jackson needs this. */
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

    /** convenience—cost + tax (not used for JSON) */
    public double getTotalCost() {
        return cost * (1 + taxRate);
    }
}
