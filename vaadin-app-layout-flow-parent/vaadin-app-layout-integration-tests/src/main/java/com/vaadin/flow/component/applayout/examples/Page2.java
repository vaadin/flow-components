package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;

@Route(value = "Page2", layout = AppRouterLayout.class)
public class Page2 extends Div {

    public Page2() {
        add(new H1("This is Page 2"));
    }
}
