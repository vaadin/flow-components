package com.vaadin.flow.component.gridpro.tests;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-pro/cell-editable-provider")
public class CellEditableProviderPage extends Div {
    public CellEditableProviderPage() {
        GridPro<Transaction> grid = new GridPro<>();
        grid.setItems(new Transaction("Transaction 1", 100, true),
                new Transaction("Transaction 2", 200, false));
        grid.addEditColumn(Transaction::getName).text(Transaction::setName);
        // Disable editing of amount and approved if transaction is approved
        grid.addEditColumn(Transaction::getAmount)
                .withCellEditableProvider(
                        transaction -> !transaction.isApproved())
                .text((item, value) -> item.setAmount(Integer.parseInt(value)));
        grid.addEditColumn(Transaction::isApproved)
                .withCellEditableProvider(
                        transaction -> !transaction.isApproved())
                .checkbox(Transaction::setApproved);
        add(grid);

        NativeButton updateData = new NativeButton("Update data", e -> {
            grid.getListDataView().getItems().forEach(transaction -> {
                transaction.setApproved(!transaction.isApproved());
            });
            grid.getDataProvider().refreshAll();
        });
        updateData.setId("update-data");
        add(updateData);

        NativeButton detach = new NativeButton("Detach", e -> {
            remove(grid);
        });
        detach.setId("detach");
        add(detach);

        NativeButton attach = new NativeButton("Attach", e -> {
            add(grid);
        });
        attach.setId("attach");
        add(attach);
    }

    public static class Transaction {
        private String name;
        private int amount;
        private boolean approved;

        public Transaction(String name, int amount, boolean approved) {
            this.name = name;
            this.amount = amount;
            this.approved = approved;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public boolean isApproved() {
            return approved;
        }

        public void setApproved(boolean approved) {
            this.approved = approved;
        }
    }
}
