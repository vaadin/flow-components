/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-checkbox/disabled-items")
public class DisabledItemsPage extends Div {

    public DisabledItemsPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setId("checkbox-group");
        checkboxGroup.setEnabled(false);

        NativeButton nativeButton = new NativeButton("add",
                event -> checkboxGroup.setItems("one", "two"));
        nativeButton.setId("add-button");

        add(checkboxGroup, nativeButton);
    }

}
