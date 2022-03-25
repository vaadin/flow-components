package com.vaadin.flow.component.login.examples;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.Route;


@Route(value = "vaadin-login/overlay-class-name")
public class OverlayClassNameView extends AbstractView {

    private final LoginOverlay login = new LoginOverlay();

    public OverlayClassNameView() {
        init(login);
        login.addClassName("custom");
        login.addLoginListener(e -> login.close());

        NativeButton button = new NativeButton("Open");
        button.setId("open");
        button.addClickListener(e -> login.setOpened(true));

        NativeButton close = new NativeButton("Close");
        close.setId("close");
        close.addClickListener(e -> login.setOpened(false));

        NativeButton addClass = new NativeButton("Add a class",
                event -> login.addClassName("added"));
        addClass.setId("add");

        NativeButton clearAllClass = new NativeButton("Clear all classes",
                event -> login.getClassNames().clear());
        clearAllClass.setId("clear");

        add(button, close, addClass, clearAllClass);
    }
}
