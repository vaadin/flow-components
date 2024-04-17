package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

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
