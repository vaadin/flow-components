package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/6035
 *
 * Lazy (server-side filtered) ComboBox with auto-open disabled. Typing a filter,
 * deleting it, then opening the dropdown is reported to leave the overlay stuck
 * on a spinner (loading loop).
 */
@Route("repro-6035")
public class Repro6035View extends Div {

    public Repro6035View() {
        // 60 items so the ComboBox pages through the (server-side) data provider.
        List<String> items = IntStream.rangeClosed(1, 60)
                .mapToObj(i -> String.format("Item %02d", i))
                .collect(Collectors.toList());

        ComboBox<String> comboBox = new ComboBox<>("Item");
        comboBox.setId("combo");
        comboBox.setAutoOpen(false);

        // Lazy, server-side filtering: each keystroke goes to the server.
        comboBox.setItems(query -> {
            String filter = query.getFilter().orElse("").toLowerCase();
            return items.stream()
                    .filter(item -> item.toLowerCase().contains(filter))
                    .skip(query.getOffset()).limit(query.getLimit());
        }, query -> {
            String filter = query.getFilter().orElse("").toLowerCase();
            return (int) items.stream()
                    .filter(item -> item.toLowerCase().contains(filter))
                    .count();
        });

        add(comboBox);
    }
}
