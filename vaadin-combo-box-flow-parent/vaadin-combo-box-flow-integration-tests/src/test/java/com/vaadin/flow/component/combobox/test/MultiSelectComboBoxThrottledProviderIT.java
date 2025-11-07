/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("multi-select-combo-box-throttled-provider")
public class MultiSelectComboBoxThrottledProviderIT
        extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
    }

    @Test
    public void selectItem_blurWhileLoading_reopen_itemsCorrectlyLoaded() {
        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        // Add a filter
        comboBox.sendKeys("Item");
        comboBox.waitForLoadingFinished();
        // Choose the first item
        comboBox.sendKeys(Keys.DOWN, Keys.ENTER, Keys.TAB);
        Assert.assertTrue(comboBox.getSelectedTexts().contains("Item 1"));

        comboBox.openPopup();
        comboBox.waitForLoadingFinished();
        Assert.assertFalse(comboBox.getOptions().isEmpty());
    }
}
