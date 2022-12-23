/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent.testbench;

import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

@Element("vaadin-cookie-consent")
public class CookieConsentElement extends TestBenchElement {

    public WebElement getDismissLinkElement() {
        return getElementFromContainer(By.className("cc-dismiss"));
    }

    public WebElement getLearnMoreLinkElement() {
        return getElementFromContainer(By.className("cc-link"));
    }

    public String getMessage() {
        return getAttribute("message");
    }

    public String getDismissLabel() {
        return getAttribute("dismiss");
    }

    public String getLearnMoreLabel() {
        return getAttribute("learnMore");
    }

    public String getLearnMoreLink() {
        return getAttribute("learnMoreLink");
    }

    public String getCookieName() {
        return getAttribute("cookieName");
    }

    public Position getPosition() {
        return Optional.ofNullable(getAttribute("position"))
                .map(value -> value.replace('-', '_')).map(String::toUpperCase)
                .map(Position::valueOf).orElse(null);
    }

    public WebElement getContainer() {
        return getDriver().findElement(By.className("cc-window"));
    }

    @Override
    public SearchContext getContext() {
        return getContainer();
    }

    @Override
    public boolean isDisplayed() {
        return getContainer().isDisplayed();
    }

    WebElement getElementFromContainer(By by) {
        final WebElement container = getContainer();
        return container != null ? container.findElement(by) : null;
    }

    /**
     * Default values for the element properties.
     *
     */
    public static final class DefaultValues {
        public static final String MESSAGE = "This website uses cookies to ensure you get the best experience.";
        public static final String DISMISS_LABEL = "Got it!";
        public static final String LEARN_MORE_LABEL = "Learn more";
        public static final String LEARN_MORE_LINK = "https://cookiesandyou.com/";
        public static final Position POSITION = Position.TOP;

        private DefaultValues() {
        }

    }
}
