package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/group-items")
public class MultiSelectComboBoxGroupItemsPage extends Div {
    public MultiSelectComboBoxGroupItemsPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton groupSelected = new NativeButton("Group selected", e -> {
            comboBox.setGroupSelectedItems(true);
        });
        groupSelected.setId("group-selected");

        NativeButton ungroupSelected = new NativeButton("Un-group selected",
                e -> {
                    comboBox.setGroupSelectedItems(false);
                });
        ungroupSelected.setId("ungroup-selected");

        add(comboBox);
        add(new Div(groupSelected, ungroupSelected));
    }
}
