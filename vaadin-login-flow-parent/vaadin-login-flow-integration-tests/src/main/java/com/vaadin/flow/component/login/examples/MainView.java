package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends AbstractView {

    private LoginForm loginForm = new LoginForm();

    public MainView() {
        init(loginForm);
    }

}
