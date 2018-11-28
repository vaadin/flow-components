package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.router.Route;

@Route
public class DisabledLoginView extends Div {

    public DisabledLoginView() {
        this.setSizeFull();
        Login login = new Login();

        login.setAction("loginAction");
        login.setEnabled(false);

        add(login);
    }
}
