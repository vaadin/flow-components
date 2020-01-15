package com.vaadin.flow.component.cookieconsent;

/*
 * #%L
 * Cookie Consent for Vaadin Flow
 * %%
 * Copyright (C) 2017 - 2019 Vaadin Ltd
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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Server-side component for the <code>vaadin-cookie-consent</code> element,
 * used for showing a cookie consent banner the first time a user visits the
 * application, until the banner is dismissed.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Tag("vaadin-cookie-consent")
@NpmPackage(value="@vaadin/vaadin-cookie-consent", version = "1.1.2")
@JsModule("@vaadin/vaadin-cookie-consent/src/vaadin-cookie-consent.js")
public class CookieConsent extends Component {

    /**
     * Creates a banner with default values.
     */
    public CookieConsent() {
    }

    /**
     * Creates a banner with the specified data
     * 
     * @param message
     *            The message to show in the banner.
     * @param dismissLabel
     *            The text to show on the dismiss/consent button.
     * @param learnMoreLabel
     *            The text to show on the 'learn more' link.
     * @param learnMoreLink
     *            The URL the 'learn more' link should open.
     * @param position
     *            Determines the position of the banner.
     */
    public CookieConsent(String message, String dismissLabel,
            String learnMoreLabel, String learnMoreLink, Position position) {
        setMessage(message);
        setDismissLabel(dismissLabel);
        setLearnMoreLabel(learnMoreLabel);
        setLearnMoreLink(learnMoreLink);
        setPosition(position);
    }

    /**
     * Sets the consent message to show in the banner.
     *
     * @param message
     *            The message to show in the banner.
     */
    public void setMessage(String message) {
        getElement().setProperty("message", message);
    }

    /**
     * Sets the dismiss/consent button's text
     *
     * @param dismissLabel
     *            The text of the button.
     */
    public void setDismissLabel(String dismissLabel) {
        getElement().setProperty("dismiss", dismissLabel);
    }

    /**
     * Sets the label of the 'learn more' link
     *
     * @param learnMoreLabel
     *            The text to show on the 'learn more' link.
     */
    public void setLearnMoreLabel(String learnMoreLabel) {
        getElement().setProperty("learnMore", learnMoreLabel);
    }

    /**
     * Sets the URL of the 'learn more' link
     *
     * @param learnMoreLink
     *            The URL the 'learn more' link should open.
     */
    public void setLearnMoreLink(String learnMoreLink) {
        getElement().setProperty("learnMoreLink", learnMoreLink);
    }

    /**
     * Sets the position of the banner on the page.
     *
     * @param position
     *            Determines the position of the banner.
     * @throws NullPointerException
     *             if position is null.
     */
    public void setPosition(Position position) {
        getElement().setProperty("position",
                position.name().toLowerCase().replace('_', '-'));
    }

    /**
     * Sets the name of the cookie to remember that the user has consented.
     *
     * This rarely needs to be changed.
     *
     * @param cookieName
     *            The name of the cookie.
     */
    public void setCookieName(String cookieName) {
        getElement().setProperty("cookieName", cookieName);
    }

    /**
     * Position of the banner. For {@link #TOP} and {@link #BOTTOM}, the banner
     * is shown with full width. For the corner positions, it is shown as a
     * smaller popup.
     */
    public enum Position {
        TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

}
