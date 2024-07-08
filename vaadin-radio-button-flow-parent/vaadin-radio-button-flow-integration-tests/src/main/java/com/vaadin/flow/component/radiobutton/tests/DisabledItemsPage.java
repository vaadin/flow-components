/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/disabled-items")
public class DisabledItemsPage extends Div {

    public DisabledItemsPage() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>();
        radioButtonGroup.setId("button-group");
        radioButtonGroup.setItemEnabledProvider("one"::equals);
        radioButtonGroup.setEnabled(false);

        NativeButton nativeButton = new NativeButton("add",
                event -> radioButtonGroup.setItems("one", "two"));
        nativeButton.setId("add-button");

        NativeButton enableButton = new NativeButton("enable",
                event -> radioButtonGroup.setEnabled(true));
        enableButton.setId("enable-button");

        NativeButton rendererEnabledButton = new NativeButton(
                "set renderer and enabled", event -> {
                    radioButtonGroup.setRenderer(
                            new TextRenderer<String>(item -> item));
                    radioButtonGroup.setEnabled(true);
                });
        rendererEnabledButton.setId("set-renderer-and-enabled-button");

        add(radioButtonGroup, nativeButton, enableButton,
                rendererEnabledButton);
    }
}
