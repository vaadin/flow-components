/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent;

import java.util.Arrays;
import java.util.Locale;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Style;

/**
 * Cookie Consent is a component for showing a cookie consent banner the first
 * time a user visits the application, until the banner is dismissed.
 * <p>
 * By default, the banner is shown at the top of the screen with a predefined
 * text, a link to cookiesandyou.com which explains what cookies are, and a
 * consent button.
 * <p>
 * Cookie Consent is fully customizable. You can customize the message, the
 * "Learn More" link, the "Dismiss" button, as well as the component’s position.
 *
 * @author Vaadin Ltd
 */
@SuppressWarnings("serial")
@Tag("vaadin-cookie-consent")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/cookie-consent", version = "24.8.0-alpha18")
@JsModule("@vaadin/cookie-consent/src/vaadin-cookie-consent.js")
@JsModule("./cookieConsentConnector.js")
public class CookieConsent extends Component implements HasStyle {

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
                position.name().toLowerCase(Locale.ENGLISH).replace('_', '-'));
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

    /**
     * @throws UnsupportedOperationException
     *             CookieConsent does not support adding styles
     */
    @Override
    public Style getStyle() {
        throw new UnsupportedOperationException(
                "CookieConsent does not support adding styles");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Store the CSS class names defined in the JS library used by the web
        // component. Exclude "cc-invisible" class set during the initial 20ms
        // animation. Preserving this class would incorrectly hide the banner.
        getElement().executeJs(
                "return this._getPopup().className.replace('cc-invisible', '').trim();")
                .then((result) -> {
                    String classValue = result.asString();
                    String[] parts = classValue.split("\\s+");
                    getClassNames().addAll(Arrays.asList(parts));

                    initConnector();
                });
    }

    private void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.cookieConsentConnector.initLazy(this)");
    }
}
