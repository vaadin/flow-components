package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        this.setSizeFull();
        Login login = new Login();

        Notification notification = new Notification("Forgot password button pressed",
                15000, Notification.Position.MIDDLE);

        login.addForgotPasswordListener(e -> notification.open());

        add(login, notification);
    }
}
