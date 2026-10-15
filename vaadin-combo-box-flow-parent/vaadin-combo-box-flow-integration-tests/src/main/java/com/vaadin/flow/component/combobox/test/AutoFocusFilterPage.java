/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/auto-focus-filter")
public class AutoFocusFilterPage extends Div {

    public AutoFocusFilterPage() {
        List<String> data = Arrays.asList("Option 2", "Option 3", "Option 4",
                "Option 5", "Another Option 2");

        ComboBox<String> comboBox = new ComboBox<>("Choose option");
        comboBox.setItems((query) -> {
            if (query.getFilter().get().isEmpty())
                return Stream.of("");
            return data.stream()
                    .filter(s -> s.contains(query.getFilter().get()))
                    .skip(query.getOffset()).limit(query.getLimit());
        }, (query) -> {
            if (query.getFilter().get().isEmpty())
                return 1;
            return (int) data.stream()
                    .filter(s -> s.contains(query.getFilter().get())).count();
        });

        comboBox.setAutofocus(true);
        this.add(comboBox);
    }
}
