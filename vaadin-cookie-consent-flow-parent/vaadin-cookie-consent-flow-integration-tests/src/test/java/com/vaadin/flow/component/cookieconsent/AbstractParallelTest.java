/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.cookieconsent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.vaadin.flow.component.cookieconsent.CookieConsent.Position;
import com.vaadin.flow.component.cookieconsent.testbench.CookieConsentElement;

import org.openqa.selenium.WebElement;

public abstract class AbstractParallelTest
        extends com.vaadin.tests.AbstractParallelTest {

    protected void verifyElement(String message, String dismissLabel,
            String learnMoreLabel, String learnMoreLink, Position position)
            throws Exception {
        final CookieConsentElement element = $(CookieConsentElement.class)
                .get(0);
        assertNotNull(element);
        assertEquals(message, element.getMessage());
        assertEquals(dismissLabel, element.getDismissLabel());
        assertEquals(learnMoreLabel, element.getLearnMoreLabel());
        assertEquals(learnMoreLink, element.getLearnMoreLink());
        assertEquals(position, element.getPosition());
        final WebElement dismiss = element.getDismissLinkElement();
        dismiss.click();
        Thread.sleep(1000);
        assertFalse(element.isDisplayed());
    }
}
