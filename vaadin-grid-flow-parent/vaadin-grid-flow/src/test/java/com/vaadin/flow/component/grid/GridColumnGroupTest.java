/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Test;

public class GridColumnGroupTest {
    @Test
    public void templateWarningSuppressed() {
        Grid<String> grid = new Grid<>();

        ColumnGroup columnGroup = new ColumnGroup(grid);

        Assert.assertTrue("Template warning is not suppressed", columnGroup
                .getElement().hasAttribute("suppress-template-warning"));
    }
}
