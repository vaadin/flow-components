
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/string-items-text-renderer")
public class StringItemsWithTextRendererIT extends AbstractComponentIT {

    @Test
    public void stringItemsAreRendered() {
        open();

        $("vaadin-combo-box").id("list").sendKeys(Keys.ARROW_DOWN);

        WebElement overlay = findElement(
                By.tagName("vaadin-combo-box-overlay"));
        List<String> items = overlay
                .findElements(By.tagName("vaadin-combo-box-item")).stream()
                .map(item -> item
                        .findElement(By.cssSelector("flow-component-renderer")))
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertEquals(
                "Unexpected items size. The rendered items size must be 2", 2,
                items.size());
        Assert.assertEquals("Unexpected rendered the first item text", "foo",
                items.get(0));
        Assert.assertEquals("Unexpected rendered the second item text", "bar",
                items.get(1));

        $("vaadin-combo-box").id("list").findElement(By.tagName("input"))
                .sendKeys(Keys.ARROW_DOWN, Keys.ENTER);

        Assert.assertEquals("Unexpected selected item text", "foo",
                $("div").id("info").getText());
    }
}
