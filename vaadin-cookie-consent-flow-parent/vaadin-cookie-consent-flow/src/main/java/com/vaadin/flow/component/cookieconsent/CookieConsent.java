package com.vaadin.flow.component.cookieconsent;

/*
 * #%L
 * Vaadin Cookie Consent for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

@SuppressWarnings("serial")
@Tag("vaadin-cookie-consent")
@HtmlImport("frontend://bower_components/vaadin-cookie-consent/vaadin-cookie-consent.html")
public class CookieConsent extends Component {

    public CookieConsent() {
    }

    public CookieConsent(String message, String dismissLabel,
            String learnMoreLabel, String learnMoreLink, Position position) {
        setMessage(message);
        setDismissLabel(dismissLabel);
        setLearnMoreLabel(learnMoreLabel);
        setLearnMoreLink(learnMoreLink);
        setPosition(position);
    }

    public void setMessage(String message) {
        getElement().setProperty("message", message);
    }

    public void setDismissLabel(String dismissLabel) {
        getElement().setProperty("dismiss", dismissLabel);
    }

    public void setLearnMoreLabel(String learnMoreLabel) {
        getElement().setProperty("learnMore", learnMoreLabel);
    }

    public void setLearnMoreLink(String learnMoreLink) {
        getElement().setProperty("learnMoreLink", learnMoreLink);
    }

    public void setPosition(Position position) {
        getElement().setProperty("position",
                position.name().toLowerCase().replace('_', '-'));
    }

    public void setCookieName(String cookieName) {
        getElement().setProperty("cookieName", cookieName);
    }

    public enum Position {
        TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
    }

}
