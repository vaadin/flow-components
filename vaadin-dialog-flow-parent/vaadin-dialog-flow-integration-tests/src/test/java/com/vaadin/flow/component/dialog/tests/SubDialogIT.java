/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/sub-dialog")
public class SubDialogIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openSubDialog_dialogContentNotChanged() {
        // Open dialog
        clickElementWithJs("open-dialog");

        // Scroll the scroller inside the dialog
        var scroller = ((TestBenchElement) findElement(By.id("scroller")));
        scroller.scroll(100);
        Assert.assertEquals("100", scroller.getPropertyString("scrollTop"));

        // Open the sub dialog
        clickElementWithJs("open-sub-dialog");

        // Expect the scroller scroll position to not have changed
        Assert.assertEquals("100", scroller.getPropertyString("scrollTop"));
    }

}
