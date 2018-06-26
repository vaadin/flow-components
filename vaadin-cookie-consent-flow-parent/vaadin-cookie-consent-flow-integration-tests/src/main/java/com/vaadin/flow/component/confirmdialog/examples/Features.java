package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;

@Route("Features")
@BodySize
public class Features extends Div {

    public Features() {
    	add(new CookieConsent());
    }

}
