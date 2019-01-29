/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.ComboBoxElementUpdated;
import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

@TestPath("set-items-later")
public class SetItemsLaterIT extends AbstractComboBoxIT {
    @Before
    public void init() {
        open();
        waitUntil(driver -> findElements(By.tagName("vaadin-combo-box"))
                .size() > 0);
    }

    @Test
    public void clickButton_comboBoxShouldContainsItems() {
        open();
        ComboBoxElement comboBox = $(ComboBoxElementUpdated.class).first();

        waitForItems(comboBox, items -> items == null);

        WebElement button = findElement(By.tagName("button"));
        button.click();

        comboBox.openPopup();

        waitForItems(comboBox, items -> items != null && items.size() == 2
                && "foo".equals(getItemLabel(items, 0))
                && "bar".equals(getItemLabel(items, 1))
        );

    }

}
