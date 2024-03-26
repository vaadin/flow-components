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
        var amount = grid.addEditColumn(Transaction::getAmount)
                .text((item, value) -> item.setAmount(Integer.parseInt(value)));
        var approved = grid.addEditColumn(Transaction::isApproved)
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

        NativeButton setProvider = new NativeButton(
                "Set cell editable provider", e -> {
                    // Disable editing of amount and approved if transaction is
                    // approved
                    ((GridPro.EditColumn<Transaction>) amount)
                            .setCellEditableProvider(
                                    transaction -> !transaction.isApproved());
                    ((GridPro.EditColumn<Transaction>) approved)
                            .setCellEditableProvider(
                                    transaction -> !transaction.isApproved());
                });
        setProvider.setId("set-provider");
        add(setProvider);

        NativeButton clearProvider = new NativeButton(
                "Clear cell editable provider", e -> {
                    ((GridPro.EditColumn<Transaction>) amount)
                            .setCellEditableProvider(null);
                    ((GridPro.EditColumn<Transaction>) approved)
                            .setCellEditableProvider(null);
                });
        clearProvider.setId("clear-provider");
        add(clearProvider);

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
