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