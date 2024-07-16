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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/component-renderer")
public class ComponentRendererIT extends AbstractComponentIT {

    @Test
    public void captionsForItemsExistWhenFirstAddingItems() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("before-renderer");

        testItems(comboBox);

    }

    @Test
    public void captionsForItemsExistWhenFirstAddingRenderer() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("after-renderer");

        testItems(comboBox);
    }

    @Test
    public void captionsForItemsExistWhenFirstSettingDataProvider() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("dp-before-renderer");

        testItems(comboBox);

    }

    @Test
    public void captionsForItemsExistWhenFirstAddingRenderer_thenDataProvider() {
        open();

        ComboBoxElement comboBox = $(ComboBoxElement.class)
                .id("dp-after-renderer");

        testItems(comboBox);
    }

    private void testItems(TestBenchElement comboBox) {
        executeScript("arguments[0].open(); return true;", comboBox);
        TestBenchElement overlay = $(TestBenchElement.class).id("overlay")
                .$(TestBenchElement.class).id("content");
        ElementQuery<TestBenchElement> items = overlay
                .$("vaadin-combo-box-item");

        Assert.assertEquals("ComboBox should always contain 3 items", 3,
                items.all().size());

        items.all().forEach(item -> Assert.assertTrue(
                "Component renderer not run as we have no VerticalLayout.",
                item.$(VerticalLayoutElement.class).exists()));
    }

    private List<?> getItems(WebElement combo) {
        List<?> items = (List<?>) getCommandExecutor()
                .executeScript("return arguments[0].items;", combo);
        return items;
    }

    private Object getItem(List<?> items, int index) {
        Map<?, ?> map = (Map<?, ?>) items.get(index);
        return map.get("label");
    }

}
