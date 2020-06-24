package com.vaadin.flow.component.select.test;

import java.util.List;

import com.vaadin.flow.component.select.testbench.SelectElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("")
public class RendererIT extends AbstractSelectIT {

    @Test
    public void testRenderer_componentRendererSet_rendersComponentsThatWork() {
        page.clickRendererButton();

        runRendererTestPattern();

        page.clickRendererButton();

        List<SelectElement.ItemElement> items = selectElement.getItems();
        Assert.assertEquals("invalid number of items", getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement itemElement = items.get(i);
            Assert.assertEquals("invalid key", i + 1 + "", itemElement.getPropertyString("value"));
            Assert.assertEquals("invalid text", "Item-" + i + "-UPDATED", itemElement.getText());
        }

    }

    private void runRendererTestPattern() {
        List<SelectElement.ItemElement> items = selectElement.getItems();

        Assert.assertEquals("Invalid number of items", getInitialNumberOfItems(), items.size());

        for (int i = 0; i < items.size(); i++) {
            SelectElement.ItemElement item = items.get(i);

            TestBenchElement span = item.findElement(By.tagName("span"));
            List<WebElement> buttons = item.findElements(By.tagName("button"));

            Assert.assertEquals(2, buttons.size());
            Assert.assertEquals("Invalid text", "Item-" + i, span.getText());
            Assert.assertEquals("Invalid button text", "Update-" + i, buttons.get(0).getText());
            Assert.assertEquals("Invalid button text", "Remove button " + i, buttons.get(1).getText());

            // remove button
            buttons.get(1).click();
            buttons = item.findElements(By.tagName("button"));
            Assert.assertEquals(1, buttons.size());

            // update click causes refreshItem which renders item again
            buttons.get(0).click();
            span = item.findElement(By.tagName("span"));
            Assert.assertEquals("Invalid text", "Item-" + i + "-UPDATED", span.getText());

        }
    }

    @Test
    public void testRenderer_initialComponentRendererSet_rendersComponentsThatWork() {
        openWithExtraParameter("renderer");

        runRendererTestPattern();
    }


    @Override
    protected int getInitialNumberOfItems() {
        return 3;
    }
}
