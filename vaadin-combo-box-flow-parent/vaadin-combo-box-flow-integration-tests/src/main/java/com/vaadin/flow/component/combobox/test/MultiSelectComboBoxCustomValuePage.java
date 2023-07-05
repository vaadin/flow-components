package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/custom-value")
public class MultiSelectComboBoxCustomValuePage extends Div {
    public MultiSelectComboBoxCustomValuePage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        List<String> items = new ArrayList<>(IntStream.range(0, 100)
                .mapToObj(i -> "item " + (i + 1)).collect(Collectors.toList()));
        comboBox.setItems(items);

        // Allow custom values, automatically add them to the data provider
        comboBox.setAllowCustomValue(true);
        comboBox.addCustomValueSetListener(e -> {
            items.add(e.getDetail());
            comboBox.getDataProvider().refreshAll();
        });

        add(comboBox);
    }
}
