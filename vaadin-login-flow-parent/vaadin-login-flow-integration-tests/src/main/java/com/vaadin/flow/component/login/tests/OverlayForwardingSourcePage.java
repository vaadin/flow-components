/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/overlay-remains-in-dom-after-detach-view")
public class OverlayForwardingSourcePage extends LoginOverlay
        implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(OverlayForwardingTargetPage.class);
    }

    public OverlayForwardingSourcePage() {
        setOpened(true);
    }
}
