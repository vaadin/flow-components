/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-combo-box/auto-focus-filter")
public class AutoFocusFilterIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void filter_itemsShouldBeThere() {
        ComboBoxElement comboBox = $(ComboBoxElement.class).first();

        comboBox.sendKeys("2");

        waitForItems(comboBox,
                items -> items.size() == 2
                        && "Option 2".equals(getItemLabel(items, 0))
                        && "Another Option 2".equals(getItemLabel(items, 1)));
    }

}
