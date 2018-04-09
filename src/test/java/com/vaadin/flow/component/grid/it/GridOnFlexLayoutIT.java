package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-on-flex-layout")
public class GridOnFlexLayoutIT extends AbstractComponentIT {

    @Test
    public void gridOccupies100PercentOfThePage() {
        open();
        getDriver().manage().window().setSize(new Dimension(600, 600));

        WebElement grid = findElement(By.id("full-size-grid"));
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
