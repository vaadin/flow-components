package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.login.Login;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div implements HasUrlParameter<String> {

    private final Login login = new Login();

    public Home() {
        this.setSizeFull();
        Notification notification = new Notification("", 15000, Notification.Position.MIDDLE);

        login.addForgotPasswordListener(e -> {
            notification.setText("Forgot password button pressed");
            notification.open();
        });

        login.addLoginListener(e -> {
            if ("username".equals(e.getUsername()) && "password".equals(e.getPassword())) {
                notification.setText("Successful login");
                notification.open();
                return;
            }

            // TODO: Set error property when implemented: https://github.com/vaadin/vaadin-login-flow/issues/9
            notification.setText("Login failed");
            notification.open();
        });

        add(login, notification);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        login.setEnabled(!"disable-login".equals(s));
    }
}
