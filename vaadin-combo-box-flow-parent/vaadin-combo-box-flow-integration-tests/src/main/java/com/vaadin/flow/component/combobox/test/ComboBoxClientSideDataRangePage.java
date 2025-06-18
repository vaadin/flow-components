/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/client-side-data-range")
public class ComboBoxClientSideDataRangePage extends Div {
    public static final int ITEMS_COUNT = 900;

    public ComboBoxClientSideDataRangePage() {
        ComboBox<String> comboBox = new ComboBox<>();
        List<String> items = new ArrayList<>();
        for (int i = 0; i < ITEMS_COUNT; i++) {
            items.add("Item " + i);
        }
        comboBox.setItems(items);

        Input setPageSizeInput = new Input();
        setPageSizeInput.setId("set-page-size");
        setPageSizeInput.setPlaceholder("Set page size");
        setPageSizeInput.addValueChangeListener(event -> {
            int pageSize = Integer.parseInt(event.getValue());
            comboBox.setPageSize(pageSize);
        });

        add(comboBox, setPageSizeInput);
    }
}
