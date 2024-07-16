/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.testbench.TestBenchElement;
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

        TestBenchElement overlay = $("vaadin-combo-box-overlay").first();
        TestBenchElement content = overlay.$("*").id("content");
        TestBenchElement selector = content.$("*").id("selector");
        List<String> items = selector.$("vaadin-combo-box-item").all().stream()
                .map(item -> item.$("flow-component-renderer").first())
                .map(WebElement::getText).collect(Collectors.toList());
        Assert.assertEquals(
                "Unexpected items size. The rendered items size must be 2", 2,
                items.size());
        Assert.assertEquals("Unexpected rendered the first item text", "foo",
                items.get(0));
        Assert.assertEquals("Unexpected rendered the second item text", "bar",
                items.get(1));

        $("vaadin-combo-box").id("list").sendKeys(Keys.ARROW_DOWN, Keys.ENTER);

        Assert.assertEquals("Unexpected selected item text", "foo",
                $("div").id("info").getText());
    }
}
