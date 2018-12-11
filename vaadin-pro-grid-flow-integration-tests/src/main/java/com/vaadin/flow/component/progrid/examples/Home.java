package com.vaadin.flow.component.progrid.examples;

import com.vaadin.flow.component.progrid.ProGrid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        add(new ProGrid());
    }
}
