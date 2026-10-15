/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-button/tooltip-defaults")
public class TooltipDefaultsPage extends Div {
    public TooltipDefaultsPage() {
        Button buttonWithTooltip = new Button("Button with tooltip");
        buttonWithTooltip.setTooltipText("Tooltip");

        NativeButton setDefaultDelaysTo2000Button = new NativeButton(
                "Set default delays to 2000", e -> {
                    TooltipConfiguration.setDefaultFocusDelay(2000);
                    TooltipConfiguration.setDefaultHoverDelay(2000);
                    TooltipConfiguration.setDefaultHideDelay(2000);
                });
        setDefaultDelaysTo2000Button.setId("set-default-delays-to-2000");

        NativeButton setDefaultDelaysTo5000Button = new NativeButton(
                "Set default delays to 5000", e -> {
                    TooltipConfiguration.setDefaultFocusDelay(5000);
                    TooltipConfiguration.setDefaultHoverDelay(5000);
                    TooltipConfiguration.setDefaultHideDelay(5000);
                });
        setDefaultDelaysTo5000Button.setId("set-default-delays-to-5000");

        add(buttonWithTooltip, new Div(setDefaultDelaysTo2000Button,
                setDefaultDelaysTo5000Button));
    }
}
