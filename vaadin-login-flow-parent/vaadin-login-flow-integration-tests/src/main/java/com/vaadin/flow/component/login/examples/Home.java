package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        add(new Login());
    }
}
