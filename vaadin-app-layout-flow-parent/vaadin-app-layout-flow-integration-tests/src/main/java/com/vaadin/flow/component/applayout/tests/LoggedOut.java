package com.vaadin.flow.component.applayout.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout/LoggedOut")
public class LoggedOut extends Div {

    public LoggedOut() {
        add(new H1("Logged out!"));
    }
}
