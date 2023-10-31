package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/all-chips")
public class MultiSelectComboBoxAllChipsPage extends Div {
    public MultiSelectComboBoxAllChipsPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton showAllChips = new NativeButton("Show all chips", e -> {
            comboBox.setAllChipsVisible(true);
        });
        showAllChips.setId("show-all-chips");

        NativeButton dontShowAllChips = new NativeButton("Don't show all chips",
                e -> {
                    comboBox.setAllChipsVisible(false);
                });
        dontShowAllChips.setId("dont-show-all-chips");

        add(comboBox);
        add(new Div(showAllChips, dontShowAllChips));
    }
}
