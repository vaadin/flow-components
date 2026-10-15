/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.popover.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

@PreserveOnRefresh
@Route("vaadin-popover/delay-defaults")
public class PopoverDelayDefaultsPage extends Div {
    public PopoverDelayDefaultsPage() {
        Popover popover = new Popover();

        NativeButton setDefaultDelaysTo2000Button = new NativeButton(
                "Set default delays to 2000", e -> {
                    Popover.setDefaultFocusDelay(2000);
                    Popover.setDefaultHoverDelay(2000);
                    Popover.setDefaultHideDelay(2000);
                });
        setDefaultDelaysTo2000Button.setId("set-default-delays-to-2000");

        NativeButton setDefaultDelaysTo5000Button = new NativeButton(
                "Set default delays to 5000", e -> {
                    Popover.setDefaultFocusDelay(5000);
                    Popover.setDefaultHoverDelay(5000);
                    Popover.setDefaultHideDelay(5000);
                });
        setDefaultDelaysTo5000Button.setId("set-default-delays-to-5000");

        add(popover, setDefaultDelaysTo2000Button,
                setDefaultDelaysTo5000Button);
    }
}
