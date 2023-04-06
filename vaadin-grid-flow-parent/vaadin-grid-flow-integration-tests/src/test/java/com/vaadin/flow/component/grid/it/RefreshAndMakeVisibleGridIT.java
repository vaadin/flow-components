
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

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
