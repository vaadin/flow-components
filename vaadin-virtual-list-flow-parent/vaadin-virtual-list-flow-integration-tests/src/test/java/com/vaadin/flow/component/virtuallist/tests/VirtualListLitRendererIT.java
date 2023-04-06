
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-virtual-list/lit-renderer")
public class VirtualListLitRendererIT extends AbstractComponentIT {

    private VirtualListElement list;

    @Before
    public void init() {
        open();
        list = $(VirtualListElement.class).first();
        waitForElementPresent(By.id("item-0"));
    }

    @Test
    public void shouldRenderFirstItem() {
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertNotNull(item);
    }

    @Test
    public void shouldRenderLastItem() {
        int rowCount = list.getRowCount();
        list.scrollToRow(rowCount - 1);
        waitForElementPresent(By.id("item-" + (rowCount - 1)));
    }

    @Test
    public void shouldSwitchToComponentRenderer() {
        clickElementWithJs("componentRendererButton");
        waitForElementPresent(By.id("item-0"));
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertEquals("Component: Item 0", item.getText());
    }

    @Test
    public void shouldSwitchBackToLitRenderer() {
        clickElementWithJs("componentRendererButton");
        clickElementWithJs("litRendererButton");
        WebElement item = list.findElement(By.id("item-0"));
        Assert.assertEquals("Lit: Item 0", item.getText());
    }

    @Test
    public void shouldClickAnItem() {
        waitForElementPresent(By.id("item-0"));
        clickElementWithJs("item-0");
        waitForElementPresent(
                By.cssSelector("vaadin-virtual-list[data-clicked-item=\"0\"]"));
    }

}
