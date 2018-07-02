package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;

@Route("DefaultSettings")
@BodySize
public class DefaultSettings extends Div {

    public DefaultSettings() {
        add(new CookieConsent());
    }

}
