/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/using-templates")
public class GridViewUsingTemplatesIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void gridWithColumnTemplate() {
        GridElement grid = $(GridElement.class).id("template-renderer");
        scrollToElement(grid);

        Assert.assertEquals("0", grid.getCell(0, 0).getText());
        var person = grid.getCell(0, 1).getContext()
                .findElement(By.cssSelector("[title=\"Person 1\"]"));

        Assert.assertEquals("Person 1\n23 years old", person.getText());
        Assert.assertEquals("Street S, number 4910795",
                grid.getCell(0, 2).getText());

        List<TestBenchElement> buttons = grid.getCell(0, 3).$("button").all();
        Assert.assertEquals("Update", buttons.get(0).getText());
        Assert.assertEquals("Remove", buttons.get(1).getText());
        Assert.assertEquals(2, buttons.size());

        buttons.get(0).click();

        var personUpdated = grid.getCell(0, 1).getContext()
                .findElement(By.cssSelector("[title='Person 1 Updated']"));
        Assert.assertEquals("Person 1 Updated\n23 years old",
                personUpdated.getText());
        buttons.get(0).click();
        var personUpdated2 = grid.getCell(0, 1).getContext().findElement(
                By.cssSelector("[title='Person 1 Updated Updated']"));
        Assert.assertEquals("Person 1 Updated Updated\n23 years old",
                personUpdated2.getText());

        buttons.get(1).click();
        var person2 = grid.getCell(0, 1).getContext()
                .findElement(By.cssSelector("[title='Person 2']"));
        Assert.assertEquals("Person 2\n61 years old", person2.getText());
    }
}
