package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.router.Route;

@Route("CustomMessages")
@BodySize
public class CustomMessages extends Div {

    public CustomMessages() {
        final String message = "We are using cookies to make your visit here awesome!";
        final String dismissLabel = "Cool!";
        final String learnMoreLabel = "Why?";
        final String learnMoreLink = "https://vaadin.com/terms-of-service";
        final Position position = Position.BOTTOM_RIGHT;
        final CookieConsent consent = new CookieConsent(message, dismissLabel,
                learnMoreLabel, learnMoreLink, position);
        consent.setCookieName("vaadinconsent-custom-cookiename");
        add(consent);
    }

}
