
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
