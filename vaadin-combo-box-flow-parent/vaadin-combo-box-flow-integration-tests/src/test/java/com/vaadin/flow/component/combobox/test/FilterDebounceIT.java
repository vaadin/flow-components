/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-combo-box/filter-debounce")
public class FilterDebounceIT extends AbstractComboBoxIT {

    private ComboBoxElement combo;
    private TestBenchElement input;

    @Before
    public void init() {
        open();
        combo = $(ComboBoxElement.class).first();
        input = $("input").first();
    }

    /**
     * vaadin/vaadin-combo-box-flow#296 - Filtering problem using slow
     * DataProvider
     */
    @Test
    public void filterDebounce_doesNotHang() {
        combo.setFilter("a");
        combo.selectByText("aaa");
        tabOutAndBackFromCombo();
        Assert.assertTrue(combo.isPopupOpen());
        combo.getCommandExecutor().waitForVaadin();
        tabOutAndBackFromCombo();
        waitForExpectedItems();
        tabOutAndBackFromCombo();
        waitForExpectedItems();
    }

    private void tabOutAndBackFromCombo() {
        combo.sendKeys("\t");
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));
        combo.sendKeys("a");
    }

    @SuppressWarnings("unchecked")
    private void waitForExpectedItems() {
        waitForItems(combo,
                items -> items.size() == 1
                        && ((Map<Object, Object>) items.get(0)).get("label")
                                .equals("aaa"));
    }
}
