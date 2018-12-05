package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends AbstractView {

    private Login login = new Login();

    public Home() {
        init(login);
    }

}
