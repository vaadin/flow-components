package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/forwarding-target")
public class OverlayForwardingTargetPage extends Div {
    public OverlayForwardingTargetPage() {
        setId("forwarded-view");
        add(new Text("Forwarded"));
    }
}