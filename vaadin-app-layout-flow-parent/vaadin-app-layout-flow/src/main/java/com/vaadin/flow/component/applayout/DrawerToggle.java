/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Server-side component for the {@code <vaadin-drawer-toggle>} element. It is a
 * button that allows the user to open and close the drawer. To use it, add it
 * to the {@link AppLayout}, typically in the navbar slot. <code>
 *     AppLayout layout = new AppLayout();
 *     layout.addToNavbar(new DrawerToggle());
 * </code>
 */
@Tag("vaadin-drawer-toggle")
@HtmlImport("frontend://bower_components/vaadin-app-layout/src/vaadin-drawer-toggle.html")
@NpmPackage(value = "@vaadin/vaadin-app-layout", version = "2.2.0")
@JsModule("@vaadin/vaadin-app-layout/src/vaadin-drawer-toggle.js")
public class DrawerToggle extends Button {

    public void setIcon(Component icon) {
        super.setIcon(icon);
        // the slot attribute needs to be removed because vaadin-drawer-toggle
        // template doesn't have prefix and suffix slots
        icon.getElement().removeAttribute("slot");
    }

}
