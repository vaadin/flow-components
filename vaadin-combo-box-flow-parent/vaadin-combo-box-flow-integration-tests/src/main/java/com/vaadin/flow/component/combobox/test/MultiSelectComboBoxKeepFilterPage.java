package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/keep-filter")
public class MultiSelectComboBoxKeepFilterPage extends Div {
    public MultiSelectComboBoxKeepFilterPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        Checkbox keepFilter = new Checkbox("Keep filter");
        keepFilter.setId("keep-filter");
        keepFilter.addValueChangeListener(e -> {
            comboBox.setKeepFilter(keepFilter.getValue());
        });

        add(comboBox, keepFilter);
    }
}
