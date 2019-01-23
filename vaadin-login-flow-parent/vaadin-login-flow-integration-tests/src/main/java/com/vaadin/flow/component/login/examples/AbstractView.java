package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractView extends Div implements HasUrlParameter<String> {

    private AbstractLogin login;

    public AbstractView() {
        this.setSizeFull();
    }

    public void init(AbstractLogin login) {
        this.login = login;
        Notification notification = new Notification("", 15000, Notification.Position.MIDDLE);

        login.addForgotPasswordListener(e -> {
            notification.setText("Forgot password button pressed");
            notification.open();
        });

        AtomicInteger failCounter = new AtomicInteger(0);
        login.addLoginListener(e -> {
            if ("username".equals(e.getUsername()) && "password".equals(e.getPassword())) {
                failCounter.set(0);
                notification.setText("Successful login");
                notification.open();
                return;
            }

            login.setError(true);
            if (failCounter.incrementAndGet() > 2) {
                login.setEnabled(false);
            }
        });

        add(login, notification);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String s) {
        login.setEnabled(!"disable-login".equals(s));

        if ("no-forgot-password".equals(s)) {
            login.setForgotPasswordButtonVisible(false);
        }
    }
}
