/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/refresh-invisible-grid")
public class RefreshAndMakeVisibleGridIT extends AbstractComponentIT {

    @Test
    public void refreshDataProviderAndMakeGridVisible() {
        open();

        $("button").id("refresh").click();
        checkLogsForErrors();

        boolean hasFooCell = $("vaadin-grid-cell-content").all().stream()
                .anyMatch(element -> "foo".equals(element.getText()));

        Assert.assertTrue(
                "Grid has no 'foo' cell after making it visible and refresh data provider",
                hasFooCell);
    }
}
