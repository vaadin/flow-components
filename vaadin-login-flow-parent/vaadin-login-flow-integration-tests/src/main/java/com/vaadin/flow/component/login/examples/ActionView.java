package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.router.Route;

@Route
public class ActionView extends Div {

    public ActionView() {
        this.setSizeFull();
        Login login = new Login();
        login.setAction("action/process-login-here");
        add(login);
    }
}
