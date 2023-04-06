
package com.vaadin.flow.data.renderer.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;

import java.util.logging.Level;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-renderer-flow/lit-renderer")
public class LitRendererIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldRenderFirstItem() {
        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("Item: 0", item.getText());
    }

    @Test
    public void shouldRenderWithNoBoundValueProviders() {
        clickElementWithJs("setSimpleLitRendererButton");
        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("0", item.getText());
    }

    @Test
    public void shouldRemoveTheRenderer() {
        clickElementWithJs("removeRendererButton");
        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("[object Object]", item.getText());
    }

    @Test
    public void shouldRenderAfterReattaching() {
        // Detach
        clickElementWithJs("toggleAttachedButton");
        // Reattach
        clickElementWithJs("toggleAttachedButton");

        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("Item: 0", item.getText());
    }

    @Test
    public void shouldNotRenderAfterReattaching() {
        // Remove renderer
        clickElementWithJs("removeRendererButton");
        // Detach
        clickElementWithJs("toggleAttachedButton");
        // Reattach
        clickElementWithJs("toggleAttachedButton");

        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("[object Object]", item.getText());
    }

    @Test
    public void shouldInvokeCallableFromEvent() {
        clickElementWithJs("content-0");
        Assert.assertEquals("event: clicked, item: 0",
                getClientCallableLogArray());
    }

    @Test
    public void shouldNotIncludeEventInCallableArguments() {
        WebElement itemContent = findElement(By.id("content-0"));
        drag(itemContent);
        Assert.assertEquals("event: dragged, item: 0, argument count: 0",
                getClientCallableLogArray());
    }

    @Test
    public void shouldInvokeCallableManually() {
        WebElement itemContent = findElement(By.id("content-0"));
        itemContent.sendKeys("a");
        Assert.assertEquals("event: keyPressed, item: 0, key: a",
                getClientCallableLogArray());
    }

    @Test
    public void shouldRemoveAndAddLitRenderer() {
        clickElementWithJs("removeRendererButton");
        clickElementWithJs("setLitRendererButton");
        WebElement item = findElement(By.id("item-0"));
        Assert.assertEquals("Item: 0", item.getText());
    }

    @Test
    public void shouldSupportRendererInstanceSpecificProperties() {
        clickElementWithJs("setDetailsLitRendererButton");
        WebElement main = findElement(By.cssSelector("#item-0 .main"));
        Assert.assertEquals("Item: 0", main.getText());
        WebElement details = findElement(By.cssSelector("#item-0 .details"));
        Assert.assertEquals("Details: 0 (details)", details.getText());
    }

    private String getClientCallableLogArray() {
        String message = getLogEntries(Level.WARNING).get(0).getMessage();
        return message.split("\"")[1];
    }

}
