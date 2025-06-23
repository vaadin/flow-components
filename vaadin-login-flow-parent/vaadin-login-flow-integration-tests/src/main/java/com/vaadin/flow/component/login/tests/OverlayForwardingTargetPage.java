/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/forwarding-target")
public class OverlayForwardingTargetPage extends Div {
    public OverlayForwardingTargetPage() {
        setId("forwarded-view");
        add(new Text("Forwarded"));
    }
}
