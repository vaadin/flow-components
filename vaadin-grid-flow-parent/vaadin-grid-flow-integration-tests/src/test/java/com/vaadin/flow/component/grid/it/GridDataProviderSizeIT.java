/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/grid-data-provider-size")
public class GridDataProviderSizeIT extends AbstractComponentIT {

    @Test
    public void sizeInBackEndCalledOnce() {
        open();

        WebElement info = $("div").id("info");
        Assert.assertEquals("sizeInBackEnd should be called once", "1",
                info.getText());
    }
}
