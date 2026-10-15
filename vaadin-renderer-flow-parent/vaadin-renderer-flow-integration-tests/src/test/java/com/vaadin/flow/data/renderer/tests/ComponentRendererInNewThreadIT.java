/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.data.renderer.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-renderer-flow/component-renderer-in-new-thread")
public class ComponentRendererInNewThreadIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void addComponentRendererBeforeAttach_componentIsRendered() {
        findElement(By.id("add-component-renderer-before-attach")).click();

        WebElement component = waitUntil(driver -> findElement(
                By.tagName("lit-renderer-test-component")));

        WebElement item0 = component.findElement(By.id("item-0"));
        Assert.assertNotNull(item0);
        Assert.assertEquals("Item", item0.getText());
    }

    @Test
    public void addComponentRendererAfterAttach_componentIsRendered() {
        findElement(By.id("add-component-renderer-after-attach")).click();

        WebElement component = waitUntil(driver -> findElement(
                By.tagName("lit-renderer-test-component")));

        WebElement item0 = component.findElement(By.id("item-0"));
        Assert.assertNotNull(item0);
        Assert.assertEquals("Item", item0.getText());
    }
}
