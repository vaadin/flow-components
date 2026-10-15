/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.card.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.card.testbench.CardElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-card")
public class CardIT extends AbstractComponentIT {

    private CardElement card;

    @Before
    public void init() {
        open();
        card = $(CardElement.class).waitForFirst();
    }

    @Test
    public void rendersCardComponent() {
        boolean hasShadowRoot = (Boolean) executeScript(
                "return arguments[0].shadowRoot !== null", card);
        String componentName = (String) executeScript(
                "return Object.getPrototypeOf(arguments[0]).constructor.is",
                card);

        Assert.assertTrue(hasShadowRoot);
        Assert.assertEquals("vaadin-card", componentName);
        Assert.assertNotNull(card.getTitle());
        Assert.assertNotNull(card.getSubtitle());
        Assert.assertNotNull(card.getMedia());
        Assert.assertNotNull(card.getHeaderPrefix());
        Assert.assertNotNull(card.getHeaderSuffix());
        Assert.assertFalse(card.getFooterContents().isEmpty());
        Assert.assertFalse(card.getContents().isEmpty());
    }
}
