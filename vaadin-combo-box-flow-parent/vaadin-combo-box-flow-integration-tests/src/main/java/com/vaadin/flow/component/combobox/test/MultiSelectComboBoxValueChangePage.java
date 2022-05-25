package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/value-change")
public class MultiSelectComboBoxValueChangePage extends Div {
    public MultiSelectComboBoxValueChangePage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);
        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("500px");

        NativeButton setServerSideValue = new NativeButton(
                "Set value server-side", e -> {
                    comboBox.setValue(Set.of("Item 1", "Item 2"));
                });
        setServerSideValue.setId("set-server-side-value");

        Span eventValue = new Span();
        eventValue.setId("event-value");
        Span eventOrigin = new Span();
        eventOrigin.setId("event-origin");
        comboBox.addValueChangeListener(e -> {
            String combinedValues = String.join(",", e.getValue());

            eventValue.setText(combinedValues);
            eventOrigin.setText(e.isFromClient() ? "client" : "server");
        });

        add(comboBox);
        add(new Div(setServerSideValue));
        add(new Div(new Span("Event value: "), eventValue));
        add(new Div(new Span("Event origin: "), eventOrigin));
    }
}
