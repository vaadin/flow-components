/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-login/login-overlay-opened-before-forwarding-target")
public class LoginOverlayOpenedBeforeForwardingTargetPage extends Div {
    public LoginOverlayOpenedBeforeForwardingTargetPage() {
        setId("forwarded-view");
        add(new Text("Forwarded"));
    }
}
