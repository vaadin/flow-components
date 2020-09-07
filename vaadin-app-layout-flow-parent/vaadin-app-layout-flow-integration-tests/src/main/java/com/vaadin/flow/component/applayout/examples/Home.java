package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-app-layout", layout = AppRouterLayout.class)
public class Home extends Div {

    public Home() {
        add(new H1("Welcome home"));
    }
}
