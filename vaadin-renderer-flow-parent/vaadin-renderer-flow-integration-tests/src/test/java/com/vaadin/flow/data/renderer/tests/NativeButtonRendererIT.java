/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
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

@TestPath("vaadin-renderer-flow/native-button-renderer")
public class NativeButtonRendererIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void shouldRender_NativeButtonRenderer() {
        WebElement element = findElement(By.id("nativeBtn"));
        Assert.assertNotNull(element);
        Assert.assertEquals("button", element.getTagName());
        Assert.assertEquals("Label Item", element.getText());
    }
}
