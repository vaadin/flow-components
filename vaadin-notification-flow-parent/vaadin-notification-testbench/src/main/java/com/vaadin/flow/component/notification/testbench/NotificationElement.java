/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.notification.testbench;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;

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
        return getPropertyElement("_card");
    }
}
