/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
