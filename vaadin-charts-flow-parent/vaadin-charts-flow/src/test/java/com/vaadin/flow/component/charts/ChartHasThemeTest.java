/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

package com.vaadin.flow.component.charts;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasTheme;

public class ChartHasThemeTest {

    @Test
    public void hasTheme() {
        Chart chart = new Chart();
        Assert.assertTrue(chart instanceof HasTheme);
    }

}
