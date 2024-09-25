/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
                getFirstClientCallableLogMessage());
    }

    @Test
    public void shouldNotInvokeCallableFromEventWhenContentDiscardedAfterRefresh() {
        // Start long refresh and click on the content-0 while the refresh is
        // still running
        this.executeScript(
                "document.getElementById('longRefreshButton').click();"
                        + "document.getElementById('content-0').click();");

        Assert.assertTrue(noClientCallableLogMessageProduced());
    }

    @Test
    public void shouldNotIncludeEventInCallableArguments() {
        WebElement itemContent = findElement(By.id("content-0"));
        drag(itemContent);
        Assert.assertEquals("event: dragged, item: 0, argument count: 0",
                getFirstClientCallableLogMessage());
    }

    @Test
    public void shouldInvokeCallableManually() {
        WebElement itemContent = findElement(By.id("content-0"));
        itemContent.sendKeys("a");
        Assert.assertEquals("event: keyPressed, item: 0, key: a",
                getFirstClientCallableLogMessage());
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

    @Test
    public void shouldBeAbleToUseLiveDirective() {
        clickElementWithJs("setCheckboxRenderer");
        WebElement checkbox = findElement(By.cssSelector("#item-0 input"));
        checkbox.click();
        clickElementWithJs("longRefreshButton");
        checkbox = findElement(By.cssSelector("#item-0 input"));
        Assert.assertFalse(checkbox.isSelected());
    }

    private String getFirstClientCallableLogMessage() {
        return getLogEntries(Level.WARNING).stream()
                // Discard all but event messages
                .filter(m -> m.getMessage().contains("event:"))
                // Return first warning message in console
                .findFirst()
                .map(logEntry -> logEntry.getMessage().split("\"")[1])
                .orElse(null);
    }

    private boolean noClientCallableLogMessageProduced() {
        return getFirstClientCallableLogMessage() == null;
    }

}
