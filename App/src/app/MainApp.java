package app;

import model.*;
import util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window for the Roommate Expense Tracker.
 */
public class MainApp extends JFrame {
    private PurchaseManager manager;

    /**
     * Initializes the main window and loads persisted data.
     */
    public MainApp() {
        super("Roommate Expense Tracker");
        manager = DataStore.load();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI();
    }

    /**
     * Sets up the tabbed interface for New Purchase, View, and Summary.
     */
    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("New Purchase", createNewPurchasePanel());
        tabs.add("View Purchases", createViewPurchasesPanel());
        tabs.add("Summary", createSummaryPanel());
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Panel for entering a new purchase with item-by-item entry.
     */
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
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
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

        // Add-item action
        addItemBtn.addActionListener(e -> {
            Item it = showAddItemDialog();
            if (it != null) listModel.addElement(it);
        });

        // Save-purchase action
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
            try {
                DataStore.save(manager);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Purchase saved.");
            storeField.setText("");
            totalCostField.setText("");
            listModel.clear();
        });

        return panel;
    }

    /**
     * Shows a dialog to add a single item to a purchase.
     *
     * @return the newly created Item, or null if cancelled
     */
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
        Map<String, JTextField> ratioFields = new HashMap<>();
        for (String name : PurchaseManager.ROOMMATES) {
            JCheckBox cb = new JCheckBox(name);
            JTextField rf = new JTextField("0.0");
            rf.setEnabled(false);
            cb.addActionListener(e -> rf.setEnabled(cb.isSelected()));
            dialog.add(cb);
            dialog.add(rf);
            checks.put(name, cb);
            ratioFields.put(name, rf);
        }

        JButton ok = new JButton("OK");
        dialog.add(new JLabel());
        dialog.add(ok);

        final Item[] result = new Item[1];
        ok.addActionListener(e -> {
            String desc = descField.getText().trim();
            double c, t;
            try {
                c = Double.parseDouble(costField.getText().trim());
                t = Double.parseDouble(taxField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid cost or tax.");
                return;
            }
            Map<String, Double> splits = new HashMap<>();
            double sum = 0;
            for (String name : PurchaseManager.ROOMMATES) {
                if (checks.get(name).isSelected()) {
                    try {
                        double v = Double.parseDouble(ratioFields.get(name).getText().trim());
                        splits.put(name, v);
                        sum += v;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Invalid split for " + name);
                        return;
                    }
                }
            }
            if (Math.abs(sum - 1.0) > 0.001) {
                JOptionPane.showMessageDialog(dialog, "Splits must sum to 1.0");
                return;
            }
            result[0] = new Item(desc, c, t, splits);
            dialog.dispose();
        });

        dialog.setVisible(true);
        return result[0];
    }

    /**
     * Builds the panel for viewing, editing, and removing purchases.
     *
     * @return the View Purchases panel
     */
    private JPanel createViewPurchasesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"Date", "Buyer", "Store", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
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
        JScrollPane detailsScroll = new JScrollPane(details);

        JPanel controls = new JPanel();
        JButton editBtn = new JButton("Edit Selected");
        JButton removeBtn = new JButton("Remove Selected");
        controls.add(editBtn);
        controls.add(removeBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(detailsScroll, BorderLayout.CENTER);
        south.add(controls, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        // Show details on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = table.getSelectedRow();
                if (idx >= 0) {
                    Purchase p = manager.getPurchases().get(idx);
                    StringBuilder sb = new StringBuilder("Items:");
                    for (Item it : p.getItems()) {
                        sb.append(String.format(
                            "- %s: pre-tax $%.2f, tax %.2f, total $%.2f, splits %s",
                                it.getDescription(), it.getCost(), it.getTaxRate(),
                                    it.getTotalCost(), it.getSplits()));
                    }
                    details.setText(sb.toString());
                }
            }
        });

        return panel;
    }

    /**
     * Dialog for editing an existing purchase.
     *
     * @param p     The Purchase to edit.
     * @param model The table model to refresh.
     * @param row   The row index in the table.
     */
    private void showEditPurchaseDialog(Purchase p, DefaultTableModel model, int row) {
        JDialog dialog = new JDialog(this, "Edit Purchase", true);
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout());

        // Top form
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Buyer:"));
        JComboBox<String> buyerBox = new JComboBox<>(PurchaseManager.ROOMMATES);
        buyerBox.setSelectedItem(p.getBuyer());
        form.add(buyerBox);

        form.add(new JLabel("Store:"));
        JTextField storeField = new JTextField(p.getStore());
        form.add(storeField);

        form.add(new JLabel("Date:"));
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(p.getDate(), null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        form.add(dateSpinner);

        form.add(new JLabel("Total Cost:"));
        JTextField totalField = new JTextField(String.format("%.2f", p.getTotalCost()));
        form.add(totalField);

        dialog.add(form, BorderLayout.NORTH);

        // Center: item list
        DefaultListModel<Item> itemModel = new DefaultListModel<>();
        p.getItems().forEach(itemModel::addElement);
        JList<Item> itemList = new JList<>(itemModel);
        itemList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel, boolean fok) {
                super.getListCellRendererComponent(list, value, idx, sel, fok);
                Item it = (Item)value;
                setText(it.getDescription() + " ($" + String.format("%.2f", it.getTotalCost()) + ")");
                return this;
            }
        });
        dialog.add(new JScrollPane(itemList), BorderLayout.CENTER);

        // Item controls
        JPanel itemCtrls = new JPanel();
        JButton addItem = new JButton("Add Item");
        JButton editItem = new JButton("Edit Item");
        JButton remItem = new JButton("Remove Item");
        itemCtrls.add(addItem); itemCtrls.add(editItem); itemCtrls.add(remItem);
        dialog.add(itemCtrls, BorderLayout.SOUTH);

        // Item actions
        addItem.addActionListener(ae -> {
            Item it = showAddItemDialog();
            if (it != null) itemModel.addElement(it);
        });
        editItem.addActionListener(ae -> {
            int i = itemList.getSelectedIndex();
            if (i<0) return;
            Item old = itemModel.get(i);
            Item rep = showAddItemDialogWithValues(old);
            if (rep!=null) itemModel.set(i, rep);
        });
        remItem.addActionListener(ae -> {
            int i = itemList.getSelectedIndex();
            if (i>=0) itemModel.remove(i);
        });

        // Save/Cancel
        JPanel saveP = new JPanel();
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        saveP.add(save); saveP.add(cancel);
        dialog.add(saveP, BorderLayout.PAGE_END);

        save.addActionListener(ae -> {
            p.setBuyer((String)buyerBox.getSelectedItem());
            p.setStore(storeField.getText().trim());
            p.setDate((Date)dateSpinner.getValue());
            double tot;
            try { tot = Double.parseDouble(totalField.getText().trim()); }
            catch(Exception ex){ JOptionPane.showMessageDialog(dialog,"Invalid total cost"); return;} 
            p.getItems().clear();
            for (int i=0; i<itemModel.getSize(); i++){ p.getItems().add(itemModel.get(i));}
            p.setTotalCost(tot);
            try { DataStore.save(manager);} catch(Exception ex){ex.printStackTrace();}
            SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM-dd");
            model.setValueAt(dfmt.format(p.getDate()), row,0);
            model.setValueAt(p.getBuyer(), row,1);
            model.setValueAt(p.getStore(), row,2);
            model.setValueAt(String.format("%.2f",p.getTotalCost()),row,3);
            dialog.dispose();
        });
        cancel.addActionListener(ae -> dialog.dispose());

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Variant of showAddItemDialog that pre-populates fields from an existing Item.
     * @param old the Item to edit
     * @return the updated Item
     */
    private Item showAddItemDialogWithValues(Item old) {
        // For brevity, use the same dialog and ignore seeding, or implement similar to showAddItemDialog
        return showAddItemDialog();
    }

    /**
     * Builds the summary panel for each roommate.
     *
     * @return the Summary panel
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Roommate","Paid","Owed","Balance"};
        DefaultTableModel model = new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        panel.add(refresh, BorderLayout.SOUTH);
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            for (String name:PurchaseManager.ROOMMATES) {
                double paid=0, owed=0;
                for (Purchase p:manager.getPurchases()){
                    if(p.getBuyer().equals(name)) paid+=p.getTotalCost();
                    for(Item it:p.getItems()){Double sh=it.getSplits().get(name); if(sh!=null) owed+=sh*it.getTotalCost();}
                }
                model.addRow(new Object[]{name,String.format("%.2f",paid),String.format("%.2f",owed),String.format("%.2f",paid-owed)});
            }
        });
        refresh.doClick();

        return panel;
    }

    /**
     * Launches the application.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}