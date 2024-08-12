/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/refresh-data-provider")
public class RefreshDataProviderPage extends Div {

    public RefreshDataProviderPage() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setId("group");

        List<String> items = new LinkedList<>(Arrays.asList("foo", "bar"));
        group.setDataProvider(new ListDataProvider<>(items));

        NativeButton button = new NativeButton("Update items", e -> {
            items.add("baz");
            items.remove(0);
            group.getDataProvider().refreshAll();
        });

        button.setId("reset");

        add(group, button);
    }
}
