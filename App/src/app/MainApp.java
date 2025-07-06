package app;

import model.*;
import util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window for the Roommate Expense Tracker.
 */
public class MainApp extends JFrame {
    private PurchaseManager manager;

    public MainApp() {
        super("Roommate Expense Tracker");
        manager = DataStore.load();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("New Purchase", createNewPurchasePanel());
        tabs.add("View Purchases", createViewPurchasesPanel());
        tabs.add("Summary", createSummaryPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createNewPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top form
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Buyer:"));
        JComboBox<String> buyerBox = new JComboBox<>(PurchaseManager.ROOMMATES);
        form.add(buyerBox);

        form.add(new JLabel("Store:"));
        JTextField storeField = new JTextField();
        form.add(storeField);

        form.add(new JLabel("Date:"));
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        form.add(dateSpinner);

        form.add(new JLabel("Total Cost:"));
        JTextField totalCostField = new JTextField();
        form.add(totalCostField);

        JButton addItemBtn = new JButton("Add Item");
        form.add(addItemBtn);
        panel.add(form, BorderLayout.NORTH);

        // Item list
        DefaultListModel<Item> listModel = new DefaultListModel<>();
        JList<Item> itemList = new JList<>(listModel);
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Item it = (Item) value;
                setText(it.getDescription() + " ($" + String.format("%.2f", it.getTotalCost()) + ")");
                return this;
            }
        });
        panel.add(new JScrollPane(itemList), BorderLayout.CENTER);

        // Save button
        JButton saveBtn = new JButton("Save Purchase");
        panel.add(saveBtn, BorderLayout.SOUTH);

        // Actions
        addItemBtn.addActionListener(e -> {
            Item it = showAddItemDialog();
            if (it != null) listModel.addElement(it);
        });

        saveBtn.addActionListener(e -> {
            String buyer = (String) buyerBox.getSelectedItem();
            String store = storeField.getText().trim();
            Date date = (Date) dateSpinner.getValue();
            double total;
            try {
                total = Double.parseDouble(totalCostField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid total cost.");
                return;
            }
            double sum = 0;
            for (int i = 0; i < listModel.getSize(); i++) {
                sum += listModel.get(i).getTotalCost();
            }
            if (Math.abs(sum - total) > 0.01) {
                JOptionPane.showMessageDialog(this,
                    String.format("Sum of item costs (%.2f) does not match total (%.2f).", sum, total));
                return;
            }
            Purchase p = new Purchase(buyer, store, date, total);
            for (int i = 0; i < listModel.getSize(); i++) {
                p.addItem(listModel.get(i));
            }
            manager.addPurchase(p);
            try { DataStore.save(manager); } catch (Exception ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(this, "Purchase saved.");
            storeField.setText("");
            totalCostField.setText("");
            listModel.clear();
        });

        return panel;
    }

    private Item showAddItemDialog() {
        JDialog dialog = new JDialog(this, "Add Item", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        dialog.setLocationRelativeTo(this);

        dialog.add(new JLabel("Description:"));
        JTextField descField = new JTextField();
        dialog.add(descField);

        dialog.add(new JLabel("Cost (pre-tax):"));
        JTextField costField = new JTextField();
        dialog.add(costField);

        dialog.add(new JLabel("Tax Rate (e.g. 0.07):"));
        JTextField taxField = new JTextField();
        dialog.add(taxField);

        dialog.add(new JLabel("Splits (must sum to 1.0):"));
        dialog.add(new JLabel("Enter ratio per roommate:"));

        Map<String, JCheckBox> checks = new HashMap<>();
        Map<String, JTextField> ratios = new HashMap<>();
        for (String name : PurchaseManager.ROOMMATES) {
            JCheckBox cb = new JCheckBox(name);
            JTextField tf = new JTextField("0.0");
            tf.setEnabled(false);
            cb.addActionListener(e -> tf.setEnabled(cb.isSelected()));
            dialog.add(cb);
            dialog.add(tf);
            checks.put(name, cb);
            ratios.put(name, tf);
        }

        JButton ok = new JButton("OK");
        dialog.add(new JLabel());
        dialog.add(ok);

        final Item[] result = new Item[1];
        ok.addActionListener(e -> {
            String des = descField.getText().trim();
            double c, t;
            try {
                c = Double.parseDouble(costField.getText().trim());
                t = Double.parseDouble(taxField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid cost or tax.");
                return;
            }
            Map<String, Double> split = new HashMap<>();
            double sum = 0;
            for (String n : PurchaseManager.ROOMMATES) {
                JTextField tf = ratios.get(n);
                if (tf.isEnabled()) {
                    try {
                        double v = Double.parseDouble(tf.getText().trim());
                        split.put(n, v);
                        sum += v;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Invalid split for " + n);
                        return;
                    }
                }
            }
            if (Math.abs(sum - 1.0) > 0.001) {
                JOptionPane.showMessageDialog(dialog, "Splits must sum to 1.0");
                return;
            }
            result[0] = new Item(des, c, t, split);
            dialog.dispose();
        });

        dialog.setVisible(true);
        return result[0];
    }

    private JPanel createViewPurchasesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Date", "Buyer", "Store", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (Purchase p : manager.getPurchases()) {
            model.addRow(new Object[]{
                df.format(p.getDate()),
                p.getBuyer(),
                p.getStore(),
                String.format("%.2f", p.getTotalCost())
            });
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextArea details = new JTextArea(6, 40);
        details.setEditable(false);
        panel.add(new JScrollPane(details), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = table.getSelectedRow();
                if (idx >= 0) {
                    Purchase p = manager.getPurchases().get(idx);
                    StringBuilder sb = new StringBuilder("Items:\n");
                    for (Item it : p.getItems()) {
                        sb.append(String.format(
                            "- %s: pre-tax $%.2f, tax %.2f, total $%.2f, splits %s%n",
                            it.getDescription(), it.getCost(), it.getTaxRate(),
                            it.getTotalCost(), it.getSplits()
                        ));
                    }
                    details.setText(sb.toString());
                }
            }
        });

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Roommate", "Paid", "Owed", "Balance"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        panel.add(refresh, BorderLayout.SOUTH);
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            for (String name : PurchaseManager.ROOMMATES) {
                double paid = 0, owed = 0;
                for (Purchase p : manager.getPurchases()) {
                    if (p.getBuyer().equals(name)) paid += p.getTotalCost();
                    for (Item it : p.getItems()) {
                        Double share = it.getSplits().get(name);
                        if (share != null) owed += share * it.getTotalCost();
                    }
                }
                model.addRow(new Object[]{
                    name,
                    String.format("%.2f", paid),
                    String.format("%.2f", owed),
                    String.format("%.2f", paid - owed)
                });
            }
        });
        refresh.doClick();

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
