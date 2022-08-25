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