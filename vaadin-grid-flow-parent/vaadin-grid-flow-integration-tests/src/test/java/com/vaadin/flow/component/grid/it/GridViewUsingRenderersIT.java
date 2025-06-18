/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/using-renderers")
public class GridViewUsingRenderersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void basicRenderers_rowsAreRenderedAsExpected() {
        GridElement grid = $(GridElement.class).id("grid-basic-renderers");
        scrollToElement(grid);
        waitUntilCellHasText(grid, "Item 1");

        Assert.assertEquals("Item 1", grid.getCell(0, 0).getText());
        Assert.assertEquals("$ 73.10", grid.getCell(0, 1).getText());
        Assert.assertTrue(
                grid.getCell(0, 2).getText().matches("1/10/18,? 11:43:59 AM"));
        Assert.assertEquals("Jan 11, 2018", grid.getCell(0, 3).getText());
        assertRendereredContent("$$$", grid.getCell(0, 4).getInnerHTML());
        Assert.assertEquals("<button>Remove</button>",
                grid.getCell(0, 5).getInnerHTML());

        Assert.assertEquals("Item 2", grid.getCell(1, 0).getText());
        Assert.assertEquals("$ 24.05", grid.getCell(1, 1).getText());
        Assert.assertTrue(
                grid.getCell(1, 2).getText().matches("1/10/18,? 11:07:31 AM"));
        Assert.assertEquals("Jan 24, 2018", grid.getCell(1, 3).getText());
        assertRendereredContent("$", grid.getCell(1, 4).getInnerHTML());
        Assert.assertEquals("<button>Remove</button>",
                grid.getCell(1, 5).getInnerHTML());
    }

    private void assertRendereredContent(String expected, String content) {
        assertThat(content,
                CoreMatchers.allOf(
                        CoreMatchers.startsWith("<flow-component-renderer"),
                        CoreMatchers.containsString(expected),
                        CoreMatchers.endsWith("</flow-component-renderer>")));
    }

    private void waitUntilCellHasText(WebElement grid, String text) {
        waitUntil(driver -> {
            List<?> cellContentTexts = (List<?>) getCommandExecutor()
                    .executeScript(
                            "return Array.from(arguments[0].querySelectorAll('vaadin-grid-cell-content')).map(cell => cell.textContent)",
                            grid);
            return cellContentTexts.contains(text);
        });
    }
}
