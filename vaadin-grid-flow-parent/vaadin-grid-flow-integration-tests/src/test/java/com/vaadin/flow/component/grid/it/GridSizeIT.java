/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;

public abstract class GridSizeIT extends AbstractComponentIT {

    public void assertGridOccupies100PercentOfThePage(WebElement grid) {
        getDriver().manage().window().setSize(new Dimension(600, 600));

        WebElement body = findElement(By.tagName("body"));
        Dimension gridDimension = grid.getSize();
        Dimension bodyDimension = body.getSize();
        Assert.assertTrue("The body dimensions should not be 0",
                bodyDimension.getWidth() > 0 && bodyDimension.getHeight() > 0);

        Assert.assertEquals(
                "The width of the grid should be " + bodyDimension.getWidth(),
                bodyDimension.getWidth(), gridDimension.getWidth());
        Assert.assertEquals(
                "The height of the grid should be " + bodyDimension.getHeight(),
                bodyDimension.getHeight(), gridDimension.getHeight());

        getDriver().manage().window().setSize(new Dimension(300, 300));
        gridDimension = grid.getSize();
        bodyDimension = body.getSize();
        Assert.assertEquals(
                "The width of the grid should be " + bodyDimension.getWidth(),
                bodyDimension.getWidth(), gridDimension.getWidth());
        Assert.assertEquals(
                "The height of the grid should be " + bodyDimension.getHeight(),
                bodyDimension.getHeight(), gridDimension.getHeight());
    }
}
