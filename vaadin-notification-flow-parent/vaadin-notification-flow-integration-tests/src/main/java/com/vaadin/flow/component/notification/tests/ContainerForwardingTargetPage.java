/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-notification/forwarding-target")
public class ContainerForwardingTargetPage extends Div {
    public ContainerForwardingTargetPage() {
        setId("forwarded-view");
        add(new Text("Forwarded"));
    }
}
