/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.login.tests;

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

        NativeButton open = new NativeButton("Open Login");
        open.setId("open-overlay-btn");
        open.addClickListener(e -> login.setOpened(true));

        NativeButton close = new NativeButton("Close Login");
        close.setId("close-overlay-btn");
        close.addClickListener(e -> login.setOpened(false));

        NativeButton addClass = new NativeButton("Add a class",
                event -> login.addClassName("added"));
        addClass.setId("add-class-btn");

        NativeButton clearAllClass = new NativeButton("Clear all classes",
                event -> login.getClassNames().clear());
        clearAllClass.setId("clear-classes-btn");

        add(open, close, addClass, clearAllClass);
    }
}
