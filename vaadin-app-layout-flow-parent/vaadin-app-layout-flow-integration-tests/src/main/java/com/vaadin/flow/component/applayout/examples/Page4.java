package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "Page4", layout = AppRouterLayoutWithRouterLinks.class)
public class Page4 extends Div {
    public Page4() {
        add(new Text("This is Page 4"));
    }
}
