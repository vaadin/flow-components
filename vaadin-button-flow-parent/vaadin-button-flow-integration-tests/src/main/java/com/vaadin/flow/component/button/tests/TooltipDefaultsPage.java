/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.TooltipConfiguration;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-button/tooltip-defaults")
public class TooltipDefaultsPage extends Div {

    public TooltipDefaultsPage() {
        // Default values for focus & hover delay
        TooltipConfiguration.setDefaultFocusDelay(500);
        TooltipConfiguration.setDefaultHoverDelay(500);

        Button button = new Button("Set tooltip default hide delay to 1000");
        // Use component-specific delay to override the default
        button.setTooltipText("Tooltip").setHoverDelay(100);

        // Dynamically change the default hide delay
        button.addClickListener(
                e -> TooltipConfiguration.setDefaultHideDelay(1000));

        add(button);
    }
}
