
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clear-items")
public class ClearItemsPage extends Div {

    public ClearItemsPage() {
        ArrayList<String> items = new ArrayList<>();
        items.add("foo");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(items);

        NativeButton setEmptyDataProvider = new NativeButton(
                "Set empty data provider", e -> comboBox.setItems());
        setEmptyDataProvider.setId("set-empty-data-provider");

        NativeButton clearAndRefreshDataProvider = new NativeButton(
                "Clear and refresh data provider", e -> {
                    items.clear();
                    comboBox.getDataProvider().refreshAll();
                });
        clearAndRefreshDataProvider.setId("clear-and-refresh-data-provider");

        add(comboBox, setEmptyDataProvider, clearAndRefreshDataProvider);
    }
}
