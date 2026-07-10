/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.tests.MockUIRule;

public class CreditsTest {

    @Rule
    public MockUIRule mockUIRule = new MockUIRule();

    @Test
    public void setHref_safeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setHref("https://vaadin.com");

        Assert.assertEquals("https://vaadin.com", credits.getHref());
    }

    @Test
    public void setHref_unsafeScheme_throws() {
        Credits credits = new Credits();

        Assert.assertThrows(IllegalArgumentException.class,
                () -> credits.setHref("javascript:alert(1)"));
    }

    @Test
    public void setUnsafeHref_unsafeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setUnsafeHref("javascript:alert(1)");

        Assert.assertEquals("javascript:alert(1)", credits.getHref());
    }
}
