/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/client-side-filtering")
public class MultiSelectComboBoxClientSideFilteringPage extends Div {
    public MultiSelectComboBoxClientSideFilteringPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        // By generating less items than the default page size of 50 we
        // automatically enable client-side filtering
        List<String> items = IntStream.range(0, 10)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        add(comboBox);
    }
}
