/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent.tests;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-cookie-consent/styling")
public class StylingPage extends Div {
    public StylingPage() {
        // Show buttons below the cookie banner
        getStyle().set("padding-top", "100px");

        CookieConsent consent = new CookieConsent();

        NativeButton addConsent = new NativeButton("Add consent",
                e -> add(consent));
        addConsent.setId("add-consent");

        NativeButton addClassEdgeless = new NativeButton("Set edgeless theme",
                e -> {
                    consent.addClassName("cc-theme-edgeless");
                });
        addClassEdgeless.setId("add-edgeless");

        NativeButton setClassBottom = new NativeButton("Set position bottom",
                e -> {
                    consent.removeClassName("cc-top");
                    consent.addClassName("cc-bottom");
                });
        setClassBottom.setId("set-bottom");

        add(addConsent, addClassEdgeless, setClassBottom);
    }
}
