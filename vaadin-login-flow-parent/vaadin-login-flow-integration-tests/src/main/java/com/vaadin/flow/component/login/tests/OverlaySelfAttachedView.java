package com.vaadin.flow.component.login.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-login/overlayselfattached")
public class OverlaySelfAttachedView extends Div {

    public OverlaySelfAttachedView() {
        LoginOverlay login = new LoginOverlay();
        login.addLoginListener(e -> login.close());
        NativeButton button = new NativeButton("open");
        button.setId("open");
        button.addClickListener(e -> login.setOpened(true));
        add(button);
    }
}
