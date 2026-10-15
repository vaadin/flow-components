/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * View used as a forwarding source when testing navigation in
 * {@link SideNavPage}.
 */
@Route("vaadin-side-nav/side-nav-forwarding-source-view")
public class SideNavForwardingSourceView extends Div
        implements BeforeEnterObserver {

    public SideNavForwardingSourceView() {
        add(new Span(
                "View to test forwarding to a page that contains vaadin-side-nav component"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(SideNavPage.class);
    }
}
