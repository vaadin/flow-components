/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.notification.testbench;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-notification&gt;</code>
 * element.
 */
@Element("vaadin-notification")
public class NotificationElement extends TestBenchElement {

    /**
     * Checks whether the notification is shown.
     *
     * @return <code>true</code> if the notification is shown,
     *         <code>false</code> otherwise
     */
    public boolean isOpen() {
        try {
            return getPropertyBoolean("opened");
        } catch (StaleElementReferenceException e) {
            // The element is no longer even attached to the DOM
            // -> it's not open
            return false;
        }
    }

    @Override
    public String getText() {
        return getCard().getText();
    }

    @Override
    public SearchContext getContext() {
        return getCard();
    }

    /**
     * Gets the card for the notification, which is where the content is added.
     *
     * @return the card for the notification
     */
    private TestBenchElement getCard() {
        var card = getPropertyElement("_card");

        try {
            // Wait until the content becomes visible
            waitUntil(driver -> {
                var hasVisibleText = !card.getText().isEmpty();
                var hasVisibleChildren = card.findElements(By.cssSelector("*"))
                        .stream().anyMatch(element -> element.isDisplayed());
                return hasVisibleText || hasVisibleChildren;
            }, 1);
        } catch (TimeoutException e) {
            // Ignore, the card may be empty on purpose
        }
        return card;
    }
}
