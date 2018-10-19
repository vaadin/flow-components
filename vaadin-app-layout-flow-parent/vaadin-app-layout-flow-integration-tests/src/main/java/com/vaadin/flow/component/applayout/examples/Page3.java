package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "Page3", layout = AppRouterLayoutWithRouterLinks.class)
public class Page3 extends Div {
    public Page3() {
        add(new Text("This is Page 3"));
    }
}
