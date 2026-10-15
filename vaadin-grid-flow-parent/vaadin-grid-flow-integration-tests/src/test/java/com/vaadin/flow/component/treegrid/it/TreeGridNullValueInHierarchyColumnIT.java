/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/" + TreeGridBasicFeaturesPage.VIEW)
public class TreeGridNullValueInHierarchyColumnIT extends AbstractTreeGridIT {

    @Test
    public void dataProviderWithNullValues_nullValueShouldBeDisplayedAsEmptyString() {
        open();
        setupTreeGrid();
        findElement(By.id("DataProviderWithNullValues")).click();

        Assert.assertEquals(3, getTreeGrid().getRowCount());
        assertCellTexts(0, 0, new String[] { "", "0 | 0", "0 | 1" });
    }
}
