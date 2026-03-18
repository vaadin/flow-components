/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasTheme;

class ChartHasThemeTest {

    @Test
    void hasTheme() {
        Chart chart = new Chart();
        Assertions.assertTrue(chart instanceof HasTheme);
    }

}
