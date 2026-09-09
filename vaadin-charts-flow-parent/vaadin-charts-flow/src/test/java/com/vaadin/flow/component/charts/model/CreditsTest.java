/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreditsTest {

    @Test
    void setHref_safeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setHref("https://vaadin.com");

        Assertions.assertEquals("https://vaadin.com", credits.getHref());
    }

    @Test
    void setHref_unsafeScheme_throws() {
        Credits credits = new Credits();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> credits.setHref("javascript:alert(1)"));
    }

    @Test
    void setUnsafeHref_unsafeScheme_hrefSet() {
        Credits credits = new Credits();
        credits.setUnsafeHref("javascript:alert(1)");

        Assertions.assertEquals("javascript:alert(1)", credits.getHref());
    }
}
