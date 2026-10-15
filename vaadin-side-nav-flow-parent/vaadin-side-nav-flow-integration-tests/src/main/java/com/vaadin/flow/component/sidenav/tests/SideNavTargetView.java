/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * View used as a target route when testing navigation in {@link SideNavPage}.
 */
@Route("vaadin-side-nav/side-nav-test-target-view")
public class SideNavTargetView extends Div {

    public SideNavTargetView() {
        NativeButton navigateToMainPage = new NativeButton(
                "Navigate to main page",
                event -> UI.getCurrent().navigate(SideNavPage.class));
        navigateToMainPage.setId("navigate-to-main-page");

        add(new Span("View to test routing from the vaadin-side-nav component"),
                navigateToMainPage);
    }
}
