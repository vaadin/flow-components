/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid-it-demo/using-renderers")
public class GridViewUsingRenderersIT extends AbstractComponentIT {

    private GridElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridElement.class).id("grid-basic-renderers");
        scrollToElement(grid);
        waitUntilCellHasText(grid, "Item 1");
    }

    @Test
    public void basicRenderers_rowsAreRenderedAsExpected() {
        Assert.assertEquals("Item 1", grid.getCell(0, 0).getText());
        Assert.assertEquals("$ 73.10", grid.getCell(0, 1).getText());
        Assert.assertTrue(
                grid.getCell(0, 2).getText().matches("1/10/18,? 11:43:59 AM"));
        Assert.assertEquals("Jan 11, 2018", grid.getCell(0, 3).getText());
        assertRendereredContent("<span>$$$</span>",
                TestHelper.stripComments(grid.getCell(0, 4).getInnerHTML()));
        Assert.assertEquals("<button>Remove</button>",
                TestHelper.stripComments(grid.getCell(0, 5).getInnerHTML()));

        Assert.assertEquals("Item 2", grid.getCell(1, 0).getText());
        Assert.assertEquals("$ 24.05", grid.getCell(1, 1).getText());
        Assert.assertTrue(
                grid.getCell(1, 2).getText().matches("1/10/18,? 11:07:31 AM"));
        Assert.assertEquals("Jan 24, 2018", grid.getCell(1, 3).getText());
        assertRendereredContent("<span>$</span>",
                TestHelper.stripComments(grid.getCell(1, 4).getInnerHTML()));
        Assert.assertEquals("<button>Remove</button>",
                TestHelper.stripComments(grid.getCell(1, 5).getInnerHTML()));
    }

    @Test
    public void swapRenderer() {
        TestBenchElement swapRenderers = $("button").id("btn-swap-renderers");
        Assert.assertEquals("$ 73.10", grid.getCell(0, 1).getText());
        Assert.assertEquals("$ 24.05", grid.getCell(1, 1).getText());
        swapRenderers.click();
        Assert.assertEquals("US$73.10", grid.getCell(0, 1).getText().trim());
        Assert.assertEquals("US$24.05", grid.getCell(1, 1).getText().trim());
        assertRendereredContent("<span style=\"color: red\">US$73.10</span>",
                TestHelper.stripComments(grid.getCell(0, 1).getInnerHTML()));
        assertRendereredContent("<span style=\"color: blue\">US$24.05</span>",
                TestHelper.stripComments(grid.getCell(1, 1).getInnerHTML()));
    }

    @Test
    public void swapRendererWithValueProvider() {
        var priceSorter = grid.getHeaderCell(1).$("vaadin-grid-sorter").first();
        // content should not get sorted as there is no value provider initially
        priceSorter.click();
        Assert.assertEquals("Item 1", grid.getCell(0, 0).getText());
        Assert.assertEquals("Item 2", grid.getCell(1, 0).getText());
        // reset sorter by clicking twice more
        priceSorter.click();
        priceSorter.click();
        // swap renderer and value provider
        TestBenchElement swapRendererWithValueProvider = $("button")
                .id("btn-swap-renderer-with-value-provider");
        swapRendererWithValueProvider.click();
        // check content is rendered using the new renderer
        Assert.assertEquals("US$73.10", grid.getCell(0, 1).getText().trim());
        Assert.assertEquals("US$24.05", grid.getCell(1, 1).getText().trim());
        assertRendereredContent("<span style=\"color: red\">US$73.10</span>",
                TestHelper.stripComments(grid.getCell(0, 1).getInnerHTML()));
        assertRendereredContent("<span style=\"color: blue\">US$24.05</span>",
                TestHelper.stripComments(grid.getCell(1, 1).getInnerHTML()));
        // now check content is ordered using the new value provider
        priceSorter.click();
        Assert.assertEquals("Item 88", grid.getCell(0, 0).getText());
        Assert.assertEquals("Item 36", grid.getCell(1, 0).getText());
        priceSorter.click();
        Assert.assertEquals("Item 58", grid.getCell(0, 0).getText());
        Assert.assertEquals("Item 69", grid.getCell(1, 0).getText());
    }

    @Test
    public void setRendererAfterSettingEditorComponent() {
        Assert.assertEquals("<b>Item 1</b>", TestHelper.stripComments(grid.getCell(0, 0).getInnerHTML()));
        ButtonElement editButton = grid.getCell(0, 6).$(ButtonElement.class).first();
        editButton.click();
        TextFieldElement editorComponent = grid.getCell(0, 0).$(TextFieldElement.class).first();
        Assert.assertEquals("Item 1", editorComponent.getValue());
    }

    @Test
    public void editorStillShowsAfterSwappingRendererDynamically() {
        TestBenchElement swapRenderers = $("button").id("btn-swap-renderers");
        // open editor
        grid.getCell(0, 6).$(ButtonElement.class).first().click();
        NumberFieldElement editorComponent = grid.getCell(0, 1).$(NumberFieldElement.class).first();
        // close editor
        grid.getCell(0, 6).$(ButtonElement.class).last().click();
        swapRenderers.click();
        Assert.assertEquals("US$73.10", grid.getCell(0, 1).getText().trim());
        // open editor, again
        grid.getCell(0, 6).$(ButtonElement.class).first().click();
        editorComponent = grid.getCell(0, 1).$(NumberFieldElement.class).first();
        Assert.assertNotNull(editorComponent);
    }

    private void assertRendereredContent(String expected, String content) {
        Assert.assertTrue(content.contains(expected));
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
