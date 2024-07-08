/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-radio-button/refresh-data-provider")
public class RefreshDataProviderPageIT extends AbstractComponentIT {

    @Test
    public void resetComponentOnDataProviderRefresh() {
        open();

        findElement(By.id("reset")).click();

        List<TestBenchElement> radioButtons = $("vaadin-radio-button").all();
        Assert.assertEquals(2, radioButtons.size());

        Assert.assertEquals("bar", radioButtons.get(0).getText());
        Assert.assertEquals("baz", radioButtons.get(1).getText());
    }
}
