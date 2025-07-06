package app;

import model.*;
import util.DataStore;
import util.ExportUtil;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main application window for the Roommate Expense Tracker with extended features.
 */
public class MainApp extends JFrame {
    /**
     * Adapter for a one-method DocumentListener.
     */
    private interface SimpleDocumentListener extends DocumentListener {
        void update();
        @Override default void insertUpdate(DocumentEvent e)  { update(); }
        @Override default void removeUpdate(DocumentEvent e)  { update(); }
        @Override default void changedUpdate(DocumentEvent e) { update(); }
    }

    private PurchaseManager manager;
    private DefaultTableModel viewModel;
    private JTable viewTable;
    private TableRowSorter<DefaultTableModel> sorter;

    public MainApp() {
        super("Roommate Expense Tracker");
        manager = DataStore.load();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("New Purchase",   createNewPurchasePanel());
        tabs.add("View Purchases", createViewPurchasesPanel());
        tabs.add("Summary",        createSummaryPanel());
        tabs.add("Settings",       createSettingsPanel());
        add(tabs);
    }

    // ----------------------------------------------------------------
    // New Purchase panel with item editing, removal, and live sum
    // ----------------------------------------------------------------
    private JPanel createNewPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // --- Form at top --- //
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Buyer:"));
        JComboBox<String> buyerBox = new JComboBox<>(
            manager.getRoommates().toArray(new String[0])
        );
        form.add(buyerBox);

        form.add(new JLabel("Store:"));
        JTextField storeField = new JTextField();
        form.add(storeField);

        form.add(new JLabel("Date:"));
        JSpinner dateSpinner = new JSpinner(
            new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        form.add(dateSpinner);

        form.add(new JLabel("Total Cost:"));
        JTextField totalField = new JTextField();
        form.add(totalField);

        JButton addItemBtn = new JButton("Add Item");
        form.add(addItemBtn);

        panel.add(form, BorderLayout.NORTH);

        // --- Center: item list + sum label --- //
        DefaultListModel<Item> itemModel = new DefaultListModel<>();
        JList<Item> itemList = new JList<>(itemModel);
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                );
                Item it = (Item) value;
                setText(
                    it.getDescription() +
                    " ($" + String.format("%.2f", it.getTotalCost()) + ")"
                );
                return this;
            }
        });

        JLabel sumLabel = new JLabel("Current sum: $0.00");
        sumLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel center = new JPanel(new BorderLayout());
        center.add(new JScrollPane(itemList), BorderLayout.CENTER);
        center.add(sumLabel, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        // --- South: item controls + save button --- //
        JPanel itemCtrls = new JPanel();
        JButton editItem = new JButton("Edit Item");
        JButton remItem  = new JButton("Remove Item");
        itemCtrls.add(editItem);
        itemCtrls.add(remItem);

        JButton saveBtn = new JButton("Save Purchase");

        JPanel south = new JPanel(new BorderLayout());
        south.add(itemCtrls, BorderLayout.NORTH);
        south.add(saveBtn, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        // --- Helper: update sum using BigDecimal for internal, format only for display --- //
        Runnable updateSum = () -> {
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < itemModel.size(); i++) {
                sum = sum.add(BigDecimal.valueOf(
                    itemModel.get(i).getTotalCost()
                ));
            }
            // display as two-decimal string only
            sumLabel.setText(String.format(
                "Current sum: $%.2f", sum.doubleValue()
            ));
        };

        // --- Actions --- //
        addItemBtn.addActionListener(e -> {
            Item it = showAddItemDialog(null);
            if (it != null) {
                itemModel.addElement(it);
                updateSum.run();
            }
        });
        remItem.addActionListener(e -> {
            int idx = itemList.getSelectedIndex();
            if (idx >= 0) {
                itemModel.remove(idx);
                updateSum.run();
            }
        });
        editItem.addActionListener(e -> {
            int idx = itemList.getSelectedIndex();
            if (idx < 0) return;
            Item old = itemModel.get(idx);
            Item repl = showAddItemDialog(old);
            if (repl != null) {
                itemModel.set(idx, repl);
                updateSum.run();
            }
        });

        saveBtn.addActionListener(e -> {
            // parse total as BigDecimal
            BigDecimal enteredTotal;
            try {
                enteredTotal = new BigDecimal(
                    totalField.getText().trim()
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid total cost");
                return;
            }

            BigDecimal rawSum = BigDecimal.ZERO;
            for (int i = 0; i < itemModel.size(); i++) {
                rawSum = rawSum.add(BigDecimal.valueOf(
                    itemModel.get(i).getTotalCost()
                ));
            }

            // allow tolerance of one cent
            if (rawSum.subtract(enteredTotal)
                      .abs()
                      .compareTo(new BigDecimal("0.01")) > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    String.format(
                        "Sum mismatch: items = $%.2f, total = $%.2f",
                        rawSum.doubleValue(),
                        enteredTotal.doubleValue()
                    )
                );
                return;
            }

            String buyer = (String) buyerBox.getSelectedItem();
            String store = storeField.getText().trim();
            Date date   = (Date) dateSpinner.getValue();
            Purchase p  = new Purchase(
                buyer, store, date, enteredTotal.doubleValue()
            );
            for (int i = 0; i < itemModel.size(); i++) {
                p.addItem(itemModel.get(i));
            }
            manager.addPurchase(p);
            try { DataStore.save(manager); }
            catch (IOException ex) { ex.printStackTrace(); }

            JOptionPane.showMessageDialog(this, "Saved");
            refreshView();
            refreshSummary();

            // reset fields
            itemModel.clear();
            updateSum.run();
            storeField.setText("");
            totalField.setText("");
        });

        return panel;
    }


    // ----------------------------------------------------------------
    // Dialog for adding/editing an item
    // ----------------------------------------------------------------
    private Item showAddItemDialog(Item old) {
        JDialog dlg = new JDialog(
            this, old == null ? "Add Item" : "Edit Item", true
        );
        dlg.setSize(400, 400);
        dlg.setLayout(new GridLayout(0, 2, 5, 5));
        dlg.setLocationRelativeTo(this);

        dlg.add(new JLabel("Description:"));
        JTextField descF = new JTextField(
            old != null ? old.getDescription() : ""
        );
        dlg.add(descF);

        dlg.add(new JLabel("Cost (pre-tax):"));
        JTextField costF = new JTextField(
            old != null ? String.valueOf(old.getCost()) : ""
        );
        dlg.add(costF);

        dlg.add(new JLabel("Tax Rate:"));
        JTextField taxF = new JTextField(
            old != null ? String.valueOf(old.getTaxRate()) : ""
        );
        dlg.add(taxF);

        dlg.add(new JLabel("Select roommates:"));
        dlg.add(new JLabel());

        Map<String, JCheckBox> cbs = new LinkedHashMap<>();
        Map<String, JTextField> rfs = new LinkedHashMap<>();
        for (String name : manager.getRoommates()) {
            JCheckBox cb = new JCheckBox(name);
            JTextField rf = new JTextField();
            rf.setEnabled(false);
            cbs.put(name, cb);
            rfs.put(name, rf);
            dlg.add(cb);
            dlg.add(rf);
            cb.addActionListener(e -> {
                long sel = cbs.values().stream()
                              .filter(JCheckBox::isSelected)
                              .count();
                if (sel > 0) {
                    double v = 1.0 / sel;
                    cbs.forEach((n, ch) -> {
                        JTextField tf = rfs.get(n);
                        if (ch.isSelected()) {
                            tf.setText(String.format("%.4f", v));
                            tf.setEnabled(true);
                        } else {
                            tf.setEnabled(false);
                        }
                    });
                }
            });
        }

        if (old != null) {
            descF.setText(old.getDescription());
            costF.setText(String.valueOf(old.getCost()));
            taxF.setText(String.valueOf(old.getTaxRate()));
            old.getSplits().forEach((n, ratio) -> {
                JCheckBox cb = cbs.get(n);
                JTextField rf = rfs.get(n);
                cb.setSelected(true);
                rf.setText(String.format("%.4f", ratio));
                rf.setEnabled(true);
            });
        }

        JButton ok = new JButton("OK");
        dlg.add(new JLabel());
        dlg.add(ok);

        final Item[] result = new Item[1];
        ok.addActionListener(e -> {
            String d = descF.getText().trim();
            double c, t;
            try {
                c = Double.parseDouble(costF.getText().trim());
                t = Double.parseDouble(taxF.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid cost/tax");
                return;
            }
            Map<String, Double> split = new HashMap<>();
            double s = 0;
            for (String n : manager.getRoommates()) {
                if (cbs.get(n).isSelected()) {
                    double v = Double.parseDouble(rfs.get(n).getText());
                    split.put(n, v);
                    s += v;
                }
            }
            if (Math.abs(s - 1.0) > 1e-3) {
                JOptionPane.showMessageDialog(dlg, "Splits must sum to 1.0");
                return;
            }
            result[0] = new Item(d, c, t, split);
            dlg.dispose();
        });

        dlg.setVisible(true);
        return result[0];
    }

    // ----------------------------------------------------------------
    // View Purchases panel with filter, export, print, edit/remove
    // ----------------------------------------------------------------
    private JPanel createViewPurchasesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"Date", "Buyer", "Store", "Total"};
        viewModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        viewTable = new JTable(viewModel);
        sorter   = new TableRowSorter<>(viewModel);
        viewTable.setRowSorter(sorter);

        panel.add(new JScrollPane(viewTable), BorderLayout.CENTER);

        JPanel north = new JPanel();
        north.add(new JLabel("Filter:"));
        JTextField filt = new JTextField(20);
        north.add(filt);
        filt.getDocument().addDocumentListener((SimpleDocumentListener)() -> {
            String t = filt.getText();
            sorter.setRowFilter(
                t.isEmpty()
                    ? null
                    : RowFilter.regexFilter("(?i)" + t)
            );
        });
        panel.add(north, BorderLayout.NORTH);

        JPanel south = new JPanel(new BorderLayout());
        JTextArea det = new JTextArea();
        det.setEditable(false);
        south.add(new JScrollPane(det), BorderLayout.CENTER);

        JPanel ctrls = new JPanel();
        JButton exp = new JButton("Export CSV");
        JButton pr  = new JButton("Print");
        JButton rem = new JButton("Remove Purchase");
        JButton edt = new JButton("Edit Purchase");
        ctrls.add(exp);
        ctrls.add(pr);
        ctrls.add(rem);
        ctrls.add(edt);
        south.add(ctrls, BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);

        refreshView();

        viewTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int rv = viewTable.getSelectedRow();
                if (rv >= 0) {
                    int mi = viewTable.convertRowIndexToModel(rv);
                    Purchase pu = manager.getPurchases().get(mi);
                    StringBuilder sb = new StringBuilder("Items:\n");
                    for (Item it : pu.getItems()) {
                        sb.append(String.format(
                            "- %s: $%.2f, splits %s\n",
                            it.getDescription(),
                            it.getTotalCost(),
                            it.getSplits()
                        ));
                    }
                    det.setText(sb.toString());
                }
            }
        });

        exp.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    ExportUtil.exportToCSV(
                      manager, fc.getSelectedFile()
                    );
                    JOptionPane.showMessageDialog(this, "Exported");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        pr.addActionListener(e -> {
            try {
                viewTable.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        });

        rem.addActionListener(e -> {
            int row = viewTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "No purchase selected.");
                return;
            }
            int mr = viewTable.convertRowIndexToModel(row);
            int choice = JOptionPane.showConfirmDialog(
                this, "Delete this purchase?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                manager.removePurchase(mr);
                try { DataStore.save(manager); }
                catch (IOException ex) { ex.printStackTrace(); }
                refreshView();
                det.setText("");
            }
        });

        edt.addActionListener(e -> {
            int row = viewTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "No purchase selected.");
                return;
            }
            int mr = viewTable.convertRowIndexToModel(row);
            Purchase p = manager.getPurchases().get(mr);
            showEditPurchaseDialog(p, viewModel, mr);
            refreshView();
        });

        return panel;
    }

    // ----------------------------------------------------------------
    // Dialog to edit existing purchase including items
    // ----------------------------------------------------------------
    private void showEditPurchaseDialog(
        Purchase p, DefaultTableModel model, int row
    ) {
        JDialog dialog = new JDialog(this, "Edit Purchase", true);
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        form.add(new JLabel("Buyer:"));
        JComboBox<String> buyerBox = new JComboBox<>(
            manager.getRoommates().toArray(new String[0])
        );
        buyerBox.setSelectedItem(p.getBuyer());
        form.add(buyerBox);

        form.add(new JLabel("Store:"));
        JTextField storeF = new JTextField(p.getStore());
        form.add(storeF);

        form.add(new JLabel("Date:"));
        JSpinner dateSp = new JSpinner(
            new SpinnerDateModel(p.getDate(), null, null,
            Calendar.DAY_OF_MONTH)
        );
        dateSp.setEditor(
            new JSpinner.DateEditor(dateSp, "yyyy-MM-dd")
        );
        form.add(dateSp);

        form.add(new JLabel("Total Cost:"));
        JTextField totalF = new JTextField(
            String.format("%.2f", p.getTotalCost())
        );
        form.add(totalF);

        dialog.add(form, BorderLayout.NORTH);

        DefaultListModel<Item> im = new DefaultListModel<>();
        p.getItems().forEach(im::addElement);
        JList<Item> list = new JList<>(im);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> l, Object v, int idx, boolean sel, boolean fok
            ) {
                super.getListCellRendererComponent(l, v, idx, sel, fok);
                Item it = (Item) v;
                setText(
                    it.getDescription() +
                    " ($" + String.format("%.2f", it.getTotalCost()) + ")"
                );
                return this;
            }
        });
        dialog.add(new JScrollPane(list), BorderLayout.CENTER);

        // Combine item controls + save/cancel
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel itemCtrls = new JPanel();
        JButton addI  = new JButton("Add Item");
        JButton editI = new JButton("Edit Item");
        JButton remI  = new JButton("Remove Item");
        itemCtrls.add(addI);
        itemCtrls.add(editI);
        itemCtrls.add(remI);
        bottom.add(itemCtrls, BorderLayout.NORTH);

        JPanel saveP = new JPanel();
        JButton saveB   = new JButton("Save");
        JButton cancelB = new JButton("Cancel");
        saveP.add(saveB);
        saveP.add(cancelB);
        bottom.add(saveP, BorderLayout.SOUTH);

        dialog.add(bottom, BorderLayout.SOUTH);

        addI.addActionListener(ae -> {
            Item it = showAddItemDialog(null);
            if (it != null) im.addElement(it);
        });
        editI.addActionListener(ae -> {
            int i = list.getSelectedIndex();
            if (i < 0) return;
            Item old = im.get(i);
            Item rep = showAddItemDialog(old);
            if (rep != null) im.set(i, rep);
        });
        remI.addActionListener(ae -> {
            int i = list.getSelectedIndex();
            if (i >= 0) im.remove(i);
        });

        saveB.addActionListener(ae -> {
            p.setBuyer((String) buyerBox.getSelectedItem());
            p.setStore(storeF.getText().trim());
            p.setDate((Date) dateSp.getValue());
            double tot;
            try {
                tot = Double.parseDouble(totalF.getText().trim());
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(dialog,"Invalid total cost");
                return;
            }
            p.getItems().clear();
            for (int i = 0; i < im.size(); i++) {
                p.addItem(im.get(i));
            }
            p.setTotalCost(tot);

            try { DataStore.save(manager); } catch(IOException ex) { ex.printStackTrace(); }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            model.setValueAt(df.format(p.getDate()), row, 0);
            model.setValueAt(p.getBuyer(), row, 1);
            model.setValueAt(p.getStore(), row, 2);
            model.setValueAt(
                String.format("%.2f", p.getTotalCost()), row, 3
            );

            dialog.dispose();
        });
        cancelB.addActionListener(ae -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ----------------------------------------------------------------
    // Summary panel with date-range settlement
    // ----------------------------------------------------------------
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea sum = new JTextArea();
        sum.setEditable(false);
        panel.add(new JScrollPane(sum), BorderLayout.CENTER);

        JPanel top = new JPanel();
        top.add(new JLabel("From:"));
        JSpinner f = new JSpinner(new SpinnerDateModel());
        ((JSpinner.DateEditor)f.getEditor())
            .getFormat().applyPattern("yyyy-MM-dd");
        top.add(f);

        top.add(new JLabel("To:"));
        JSpinner t = new JSpinner(new SpinnerDateModel());
        ((JSpinner.DateEditor)t.getEditor())
            .getFormat().applyPattern("yyyy-MM-dd");
        top.add(t);

        JButton g = new JButton("Generate");
        top.add(g);
        panel.add(top, BorderLayout.NORTH);

        g.addActionListener(e ->
            sum.setText(computeSettlement(
                (Date)f.getValue(), (Date)t.getValue()
            ))
        );
        g.doClick();

        return panel;
    }

    // ----------------------------------------------------------------
    // Settings panel for managing roommates
    // ----------------------------------------------------------------
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<String> rmModel = new DefaultListModel<>();
        manager.getRoommates().forEach(rmModel::addElement);
        JList<String> rmList = new JList<>(rmModel);
        panel.add(new JScrollPane(rmList), BorderLayout.CENTER);

        JPanel ctrls = new JPanel();
        JButton add = new JButton("Add");
        JButton rem = new JButton("Remove");
        ctrls.add(add);
        ctrls.add(rem);
        panel.add(ctrls, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Name:");
            if (name != null && !name.trim().isEmpty()) {
                manager.addRoommate(name);
                rmModel.addElement(name);
                try { DataStore.save(manager); }
                catch (IOException ex) { ex.printStackTrace(); }
            }
        });
        rem.addActionListener(e -> {
            int idx = rmList.getSelectedIndex();
            if (idx >= 0) {
                String name = rmModel.remove(idx);
                manager.removeRoommate(name);
                try { DataStore.save(manager); }
                catch (IOException ex) { ex.printStackTrace(); }
            }
        });

        return panel;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private void refreshView() {
        viewModel.setRowCount(0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (Purchase p : manager.getPurchases()) {
            viewModel.addRow(new Object[]{
                df.format(p.getDate()),
                p.getBuyer(),
                p.getStore(),
                String.format("%.2f", p.getTotalCost())
            });
        }
    }

    private void refreshSummary() {
        // no-op
    }

    /**
     * Computes settlement instructions between roommates over a date range.
     */
    private String computeSettlement(Date from, Date to) {
        Map<String, Double> net = new HashMap<>();
        for (String r : manager.getRoommates()) {
            net.put(r, 0.0);
        }
        for (Purchase p : manager.getPurchases()) {
            if (!p.getDate().before(from) && !p.getDate().after(to)) {
                for (Item it : p.getItems()) {
                    for (Map.Entry<String, Double> e : it.getSplits().entrySet()) {
                        net.put(
                            e.getKey(),
                            net.get(e.getKey())
                            - e.getValue() * it.getTotalCost()
                        );
                    }
                }
                net.put(
                    p.getBuyer(),
                    net.get(p.getBuyer()) + p.getTotalCost()
                );
            }
        }
        java.util.List<String> debtors   = new java.util.ArrayList<>();
        java.util.List<String> creditors = new java.util.ArrayList<>();
        for (Map.Entry<String, Double> e : net.entrySet()) {
            if (e.getValue() < -1e-2) debtors.add(e.getKey());
            else if (e.getValue() >  1e-2) creditors.add(e.getKey());
        }

        StringBuilder sb = new StringBuilder("Settlements:\n");
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            String d = debtors.get(i), c = creditors.get(j);
            double owe = -net.get(d), cred = net.get(c);
            double amt = Math.min(owe, cred);
            sb.append(String.format("%s pays %s $%.3f\n", d, c, amt));
            net.put(d, net.get(d) + amt);
            net.put(c, net.get(c) - amt);
            if (Math.abs(net.get(d)) < 1e-2) i++;
            if (Math.abs(net.get(c)) < 1e-2) j++;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
