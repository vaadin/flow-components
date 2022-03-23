package com.vaadin.flow.component.cookieconsent.examples;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-cookie-consent/DefaultSettings")
public class DefaultSettings extends Div {

    public DefaultSettings() {
        add(new CookieConsent());
    }

}
