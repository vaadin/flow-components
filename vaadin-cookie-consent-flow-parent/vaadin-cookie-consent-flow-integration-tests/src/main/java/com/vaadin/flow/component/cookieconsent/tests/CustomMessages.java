/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent.tests;

import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-cookie-consent/CustomMessages")
public class CustomMessages extends Div {
    public static final String MESSAGE = "We are using cookies to make your visit here awesome!";
    public static final String DISMISS_LABEL = "Cool!";
    public static final String LEARN_MORE_LABEL = "Why?";
    public static final String LEARN_MORE_LINK = "https://vaadin.com/terms-of-service";
    public static final Position POSITION = Position.BOTTOM_RIGHT;

    public CustomMessages() {
        final CookieConsent consent = new CookieConsent(MESSAGE, DISMISS_LABEL,
                LEARN_MORE_LABEL, LEARN_MORE_LINK, POSITION);
        consent.setCookieName("vaadinconsent-custom-cookiename");
        add(consent);
    }

}
