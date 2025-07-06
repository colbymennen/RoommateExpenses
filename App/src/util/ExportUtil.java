package util;

import model.PurchaseManager;
import model.Purchase;
import model.Item;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility for exporting data in CSV or summary text.
 */
public class ExportUtil {
    /**
     * Exports purchases to a CSV file.
     */
    public static void exportToCSV(PurchaseManager mgr, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Date,Buyer,Store,Description,Cost,TaxRate,Total,SplitMap");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (Purchase p : mgr.getPurchases()) {
                String date = df.format(p.getDate());
                for (Item it : p.getItems()) {
                    pw.printf(
                        "%s,%s,%s,\"%s\",%.2f,%.2f,%.2f,\"%s\"%n",
                        date,
                        p.getBuyer(),
                        p.getStore(),
                        it.getDescription(),
                        it.getCost(),
                        it.getTaxRate(),
                        it.getTotalCost(),
                        it.getSplits().toString()
                    );
                }
            }
        }
    }

    /**
     * Exports settlement summary to a text file.
     */
    public static void exportSummary(PurchaseManager mgr, File file) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // Determine date range
        Date from = null, to = null;
        for (Purchase p : mgr.getPurchases()) {
            if (from == null || p.getDate().before(from)) from = p.getDate();
            if (to   == null || p.getDate().after(to))   to   = p.getDate();
        }
        // Compute net balances
        Map<String, Double> net = new HashMap<>();
        for (String r : mgr.getRoommates()) net.put(r, 0.0);
        for (Purchase p : mgr.getPurchases()) {
            for (Item it : p.getItems()) {
                for (Map.Entry<String, Double> e : it.getSplits().entrySet()) {
                    String name = e.getKey();
                    double share = e.getValue() * it.getTotalCost();
                    net.put(name, net.get(name) - share);
                }
            }
            net.put(
                p.getBuyer(),
                net.get(p.getBuyer()) + p.getTotalCost()
            );
        }
        // Separate debtors and creditors
        List<String> debtors   = new ArrayList<>();
        List<String> creditors = new ArrayList<>();
        for (Map.Entry<String, Double> e : net.entrySet()) {
            if      (e.getValue() < -1e-2) debtors.add(e.getKey());
            else if (e.getValue() >  1e-2) creditors.add(e.getKey());
        }
        // Write file
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Settlement Summary");
            pw.printf("Date range: %s to %s%n",
                df.format(from), df.format(to)
            );
            int i = 0, j = 0;
            while (i < debtors.size() && j < creditors.size()) {
                String d = debtors.get(i);
                String c = creditors.get(j);
                double owe = -net.get(d);
                double cred = net.get(c);
                double amt = Math.min(owe, cred);
                pw.printf("%s pays %s $%.2f%n", d, c, amt);
                net.put(d, net.get(d) + amt);
                net.put(c, net.get(c) - amt);
                if (Math.abs(net.get(d)) < 1e-2) i++;
                if (Math.abs(net.get(c)) < 1e-2) j++;
            }
        }
    }
}