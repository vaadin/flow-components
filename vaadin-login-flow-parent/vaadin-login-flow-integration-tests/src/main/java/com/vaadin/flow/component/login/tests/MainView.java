package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-login")
public class MainView extends AbstractView {

    private LoginForm loginForm = new LoginForm();

    public MainView() {
        init(loginForm);
    }

}
